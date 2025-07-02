package com.posprinter.printdemo.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.jeremyliao.liveeventbus.LiveEventBus
import com.posprinter.printdemo.App
import com.posprinter.printdemo.R
import com.posprinter.printdemo.databinding.ActivityFirstBinding
import com.posprinter.printdemo.utils.Constant
import com.posprinter.printdemo.utils.UIUtils
import com.posprinter.printdemo.widget.DlgUsbSelect
import net.posprinter.POSConnect

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityFirstBinding
    private var pos = 0
    private val launcher =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val mac = result.data!!.getStringExtra(SelectBluetoothActivity.INTENT_MAC)
                bind.addressTv.text = mac
            }
        }
    private val netLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            bind.addressEt.setText(result.data!!.getStringExtra(SelectNetActivity.MAC_ADDRESS))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(bind.root)
        initListener()
        try {
            val entries = POSConnect.getSerialPort()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, entries)
            bind.serialPortNs.adapter = adapter
        } catch (_: Exception) {

        }
        LiveEventBus.get<Boolean>(Constant.EVENT_CONNECT_STATUS).observeForever {
            refreshButton(it)
        }
    }

    private fun initListener() {
        bind.buttonConnect.setOnClickListener {
            refreshButton(false)
            when (pos) {
                0 -> connectUSB()
                1 -> connectNet()
                2 -> connectBt()
                3 -> {
                    if (bind.serialPortNs.selectedItem == null) {
                        UIUtils.toast(R.string.cannot_find_serial)
                    } else {
                        connectSerial(
                            bind.serialPortNs.selectedItem.toString(),
                            bind.baudRateNs.selectedItem.toString()
                        )
                    }
                }
                4 -> {
                    connectMAC()
                }
            }
        }
        bind.buttonDisconnect.setOnClickListener {
            refreshButton(false)
            App.get().curConnect?.close()
        }
        bind.buttonpos.setOnClickListener {
            val intent = Intent(this, PosActivity::class.java)
            startActivity(intent)
        }
        bind.buttonTspl.setOnClickListener {
            val intent = Intent(this, TsplActivity::class.java)
            startActivity(intent)
        }
        bind.cpclBtn.setOnClickListener {
            startActivity(Intent(this, CpclActivity::class.java))
        }
        bind.zplBtn.setOnClickListener {
            startActivity(Intent(this, ZplActivity::class.java))
        }
        bind.addressTv.setOnClickListener {
            when (pos) {
                2 -> launcher.launch(Intent(this, SelectBluetoothActivity::class.java))
                0 -> DlgUsbSelect(this, POSConnect.getUsbDevices(this)) { s ->
                    bind.addressTv.text = s
                }.show()
            }
        }
        bind.searchIv.setOnClickListener { netLauncher.launch(Intent(this, SelectNetActivity::class.java)) }
        bind.portSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                pos = i
                bind.baudRateNs.visibility = if (i == 3) View.VISIBLE else View.GONE
                bind.serialPortNs.visibility = if (i == 3) View.VISIBLE else View.GONE
                bind.addressEt.visibility = if (i == 1 || i == 4) View.VISIBLE else View.GONE
                bind.addressTv.visibility = if (i == 2 || i == 0) View.VISIBLE else View.GONE
                bind.searchIv.visibility = if (i == 4) View.VISIBLE else View.GONE
                if (i == 0) {
                    searchUsb()
                } else if (i == 1) {
                    bind.addressEt.setText("192.168.1.10")
                } else if (i == 2) {
                    bind.addressTv.text = ""
                } else if (i == 4) {
                    bind.addressEt.setText("")
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
        bind.escMulBtn.setOnClickListener {
            startActivity(Intent(this, EscMultiConnectionActivity::class.java))
        }
    }

    private fun refreshButton(connect: Boolean) {
        bind.buttonDisconnect.isEnabled = connect
        bind.buttonpos.isEnabled = connect
        bind.buttonTspl.isEnabled = connect
        bind.cpclBtn.isEnabled = connect
        bind.zplBtn.isEnabled = connect
    }

    //net connection
    private fun connectNet() {
        val ipAddress = bind.addressEt.text.toString()
        if (ipAddress == "") {
            UIUtils.toast(R.string.none_ipaddress)
        } else {
            App.get().connectNet(ipAddress)
        }
    }

    //USB connection
    private fun connectUSB() {
        val usbAddress = bind.addressTv.text.toString()
        if (usbAddress == "") {
            UIUtils.toast(R.string.usb_select)
        } else {
            App.get().connectUSB(usbAddress)
        }
    }

    // MAC connection. Only supports receipt printers with network port
    private fun connectMAC() {
        val macAddress = bind.addressEt.text.toString()
        if (macAddress == "") {
            UIUtils.toast(R.string.none_mac_address)
        } else {
            App.get().connectMAC(macAddress)
        }
    }

    //bluetooth connection
    private fun connectBt() {
        val bleAddress = bind.addressTv.text.toString()
        if (bleAddress == "") {
            UIUtils.toast(R.string.bt_select)
        } else {
            App.get().connectBt(bleAddress)
        }
    }

    private fun connectSerial(port: String, boudrate: String) {
        App.get().connectSerial(port, boudrate)
    }

    private fun searchUsb(): String {
        val usbNames = POSConnect.getUsbDevices(this)
        var ret = ""
        if (usbNames.isNotEmpty()) {
            ret = usbNames[0]
        }
        bind.addressTv.text = ret
        return ret
    }

    override fun onDestroy() {
        super.onDestroy()
        App.get().curConnect?.close()
    }
}