package com.posprinter.printdemo.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.posprinter.printdemo.R
import com.posprinter.printdemo.databinding.ActivityMultiConnectionBinding
import com.posprinter.printdemo.utils.UIUtils
import com.posprinter.printdemo.widget.DlgUsbSelect
import net.posprinter.IDeviceConnection
import net.posprinter.POSConnect
import net.posprinter.POSConst
import net.posprinter.POSPrinter

class EscMultiConnectionActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMultiConnectionBinding
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val mac = result.data!!.getStringExtra(SelectBluetoothActivity.INTENT_MAC)
                addressTvs[opIndex].text = mac
            }
        }
    private val portSps by lazy {
        arrayOf(bind.portSp0, bind.portSp1, bind.portSp2, bind.portSp3, bind.portSp4)
    }
    private val addressTvs by lazy {
        arrayOf(bind.addressTv0, bind.addressTv1, bind.addressTv2, bind.addressTv3, bind.addressTv4)
    }
    private val addressEts by lazy {
        arrayOf(bind.addressEt0, bind.addressEt1, bind.addressEt2, bind.addressEt3, bind.addressEt4)
    }
    private val serialPortNs by lazy {
        arrayOf(
            bind.serialPortNs0,
            bind.serialPortNs1,
            bind.serialPortNs2,
            bind.serialPortNs3,
            bind.serialPortNs4
        )
    }
    private val baudRateNs by lazy {
        arrayOf(
            bind.baudRateNs0,
            bind.baudRateNs1,
            bind.baudRateNs2,
            bind.baudRateNs3,
            bind.baudRateNs4
        )
    }
    private val connectTv by lazy {
        arrayOf(bind.connectTv0, bind.connectTv1, bind.connectTv2, bind.connectTv3, bind.connectTv4)
    }
    private val disconnectTv by lazy {
        arrayOf(
            bind.disconnectTv0,
            bind.disconnectTv1,
            bind.disconnectTv2,
            bind.disconnectTv3,
            bind.disconnectTv4
        )
    }
    private val printTestTv by lazy {
        arrayOf(
            bind.printTestTv0,
            bind.printTestTv1,
            bind.printTestTv2,
            bind.printTestTv3,
            bind.printTestTv4
        )
    }

    private val connections: Array<IDeviceConnection?> = Array(5) {
        null
    }
    private val deviceTypes = arrayOf(
        POSConnect.DEVICE_TYPE_ETHERNET,
        POSConnect.DEVICE_TYPE_USB,
        POSConnect.DEVICE_TYPE_BLUETOOTH,
        POSConnect.DEVICE_TYPE_SERIAL
    )
    private var opIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMultiConnectionBinding.inflate(layoutInflater)
        setContentView(bind.root)

        initListener()
    }

    private fun initListener() {
        portSps.forEachIndexed { index, appCompatSpinner ->
            appCompatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    appCompatSpinner.tag = i
                    baudRateNs[index].visibility = if (i == 3) View.VISIBLE else View.GONE
                    serialPortNs[index].visibility = if (i == 3) View.VISIBLE else View.GONE
                    addressEts[index].visibility = if (i == 0 || i == 4) View.VISIBLE else View.GONE
                    addressTvs[index].visibility = if (i == 2 || i == 1) View.VISIBLE else View.GONE
                    if (i == 0) {
                        addressEts[index].setText("192.168.1.10")
                    } else if (i == 1 || i == 2) {
                        addressTvs[index].text = ""
                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }
        }
        addressTvs.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                opIndex = index
                if ((portSps[index].tag.toString().toInt() == 2)) {
                    launcher.launch(Intent(this, SelectBluetoothActivity::class.java))
                } else {
                    DlgUsbSelect(this, POSConnect.getUsbDevices(this)) { s ->
                        addressTvs[index].text = s
                    }.show()
                }
            }
        }
        connectTv.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                connect(index, portSps[index].tag.toString().toInt())
            }
        }
        disconnectTv.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                connections[index]?.close()
                disconnectTv[index].isEnabled = false
                printTestTv[index].isEnabled = false
            }
        }
        printTestTv.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                printText(index)
            }
        }
    }

    private fun printText(index: Int) {
        val str = "Welcome to the printer,this is print test content!\n"
        POSPrinter(connections[index]).initializePrinter()
            .printString(str)
            .printText(
                "printText Demo\n",
                POSConst.ALIGNMENT_CENTER,
                POSConst.FNT_BOLD or POSConst.FNT_UNDERLINE,
                POSConst.TXT_1WIDTH or POSConst.TXT_2HEIGHT
            )
            .cutHalfAndFeed(1)
    }

    private fun connect(index: Int, portIndex: Int) {
        connections[index] = POSConnect.createDevice(deviceTypes[portIndex])
        val info = getConnectInfo(index, portIndex)
        if (info.isEmpty()) {
            UIUtils.toast(R.string.please_device)
        } else {
            connections[index]!!.connect(info) { code: Int, msg: String ->
                connectListener(index, code)
            }
        }
    }

    private fun getConnectInfo(index: Int, portIndex: Int): String {
        var ret = ""

        when (portIndex) {
            0 -> {
                ret = addressEts[index].text.toString()
            }
            1 -> {
                ret = addressTvs[index].text.toString()
            }
            2 -> {
                ret = addressTvs[index].text.toString()
            }
            3 -> {
                if (serialPortNs[index].selectedItem != null) {
                    ret = "${serialPortNs[index].selectedItem},${baudRateNs[index].selectedItem}"
                }
            }
        }

        return ret
    }

    private fun connectListener(index: Int, code: Int){
        if (code == POSConnect.CONNECT_SUCCESS) {
            disconnectTv[index].isEnabled = true
            printTestTv[index].isEnabled = true
        }else if (code == POSConnect.CONNECT_INTERRUPT){
            disconnectTv[index].isEnabled = false
            printTestTv[index].isEnabled = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connections.forEach {
            it?.close()
        }
    }

}