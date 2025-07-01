package com.posprinter.printdemo

import android.app.Application
import com.jeremyliao.liveeventbus.LiveEventBus
import com.posprinter.printdemo.utils.Constant
import com.posprinter.printdemo.utils.UIUtils
import net.posprinter.IConnectListener
import net.posprinter.IDeviceConnection
import net.posprinter.IPOSListener
import net.posprinter.POSConnect

/**
 * @author: star
 * @date: 2022-04-26
 */
class App : Application() {
    private val connectListener = IConnectListener { code,connInfo, msg ->
        when (code) {
            POSConnect.CONNECT_SUCCESS -> {
                UIUtils.toast(R.string.con_success)
                LiveEventBus.get<Boolean>(Constant.EVENT_CONNECT_STATUS).post(true)
            }
            POSConnect.CONNECT_FAIL -> {
                UIUtils.toast(R.string.con_failed)
                LiveEventBus.get<Boolean>(Constant.EVENT_CONNECT_STATUS).post(false)
            }
            POSConnect.CONNECT_INTERRUPT -> {
                UIUtils.toast(R.string.con_has_disconnect)
                LiveEventBus.get<Boolean>(Constant.EVENT_CONNECT_STATUS).post(false)
            }
            POSConnect.SEND_FAIL -> {
                UIUtils.toast(R.string.send_failed)
            }
            POSConnect.USB_DETACHED -> {
                UIUtils.toast(R.string.usb_detached)
            }
            POSConnect.USB_ATTACHED -> {
                UIUtils.toast(R.string.usb_attached)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        POSConnect.init(this)
    }

    var curConnect: IDeviceConnection? = null

    fun connectUSB(pathName: String) {
        curConnect?.close()
        curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_USB)
        curConnect!!.connect(pathName, connectListener)
    }

    fun connectNet(ipAddress: String) {
        curConnect?.close()
        curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_ETHERNET)
        curConnect!!.connect(ipAddress, connectListener)
    }

    fun connectBt(macAddress: String) {
        curConnect?.close()
        curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_BLUETOOTH)
        curConnect!!.connect(macAddress, connectListener)
    }
    fun connectMAC(macAddress: String) {
        curConnect?.close()
        curConnect = POSConnect.connectMac(macAddress, connectListener)
    }

    fun connectSerial(port: String, boudrate: String) {
        curConnect?.close()
        curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_SERIAL)
        curConnect!!.connect("$port,$boudrate", connectListener)
    }

    companion object {
        private lateinit var app: App

        fun get(): App {
            return app
        }
    }
}