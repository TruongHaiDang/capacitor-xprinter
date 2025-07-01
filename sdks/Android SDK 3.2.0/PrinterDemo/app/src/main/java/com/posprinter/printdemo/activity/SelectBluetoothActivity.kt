package com.posprinter.printdemo.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.posprinter.printdemo.R
import com.posprinter.printdemo.databinding.ActivitySelectBlutoothBinding
import com.posprinter.printdemo.utils.UIUtils
import com.tbruyelle.rxpermissions3.RxPermissions
import io.reactivex.rxjava3.core.Flowable
import java.util.concurrent.TimeUnit


/**
 * @author: star
 * @date: 2022-04-26
 */
@SuppressLint("MissingPermission", "NotifyDataSetChanged")
class SelectBluetoothActivity : AppCompatActivity() {
    private lateinit var bind: ActivitySelectBlutoothBinding
    private val GPS_ACTION = "android.location.PROVIDERS_CHANGED"

    companion object {
        const val INTENT_MAC = "MAC"
    }

    private val datas = ArrayList<BtAdapter.Bean>()
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private val launcher =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                requestPermission()
            } else {
                UIUtils.toast(R.string.request_permission_fail)
            }
        }
    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val btd = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
 	            if (btd?.type == 2) return
                if (btd!!.bondState != BluetoothDevice.BOND_BONDED && !deviceIsExist(btd.address)) {
                    var name = btd.name
                    if (name == null) name = "none"
                    datas.add(BtAdapter.Bean(false, name, btd.address))
                    bind.recyclerView.adapter?.notifyItemChanged(datas.size - 1)
                }
            } else if (action == GPS_ACTION) {
                if (isGpsOpen()) {
                    setBluetooth()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySelectBlutoothBinding.inflate(layoutInflater)
        setContentView(bind.root)
        bind.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = BtAdapter(datas)
        adapter.itemClick = {
            val intent = Intent()
            intent.putExtra(INTENT_MAC, datas[it].mac)
            setResult(RESULT_OK, intent)
            finish()
        }
        bind.recyclerView.adapter = adapter

        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(GPS_ACTION)
        registerReceiver(mBroadcastReceiver, intentFilter)
        requestPermission()
        initClick()
    }

    private fun initClick() {
        bind.refreshTv.setOnClickListener {
            requestPermission()
        }
    }

    private fun requestPermission() {
        val observable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RxPermissions(this)
                .request(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
        } else {
            RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        observable.subscribe { granted ->
            if (granted) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || isGpsOpen()) {
                    setBluetooth()
                } else {
                    openGPS()
                }
            } else {
                UIUtils.toast(getString(R.string.request_permission_fail))
            }
        }
    }

    private var lastTime = 0L
    private fun setBluetooth() {
        if (System.currentTimeMillis() - lastTime < 1000) {
            return
        }
        lastTime = System.currentTimeMillis()
        if (!bluetoothAdapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            launcher.launch(intent)
        } else {
            searchDevices()
        }
    }

    private fun searchDevices() {
        datas.clear()
        val device: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        device.forEach {
            val name = it.name ?: "none"
            datas.add(BtAdapter.Bean(true, name, it.address))
        }
        bind.recyclerView.adapter?.notifyDataSetChanged()
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        Flowable.timer(300, TimeUnit.MILLISECONDS)
            .subscribe {
                bluetoothAdapter.startDiscovery()
            }
    }

    private fun deviceIsExist(mac: String): Boolean {
        datas.forEach {
            if (it.mac == mac) {
                return true
            }
        }
        return false
    }

    private fun isGpsOpen(): Boolean {
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val network: Boolean = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gps || network
    }

    private fun openGPS() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        unregisterReceiver(mBroadcastReceiver)
    }

}

