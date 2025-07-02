package com.posprinter.printdemo.widget

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import com.posprinter.printdemo.App
import com.posprinter.printdemo.R
import com.posprinter.printdemo.databinding.DlgModifyNetBinding
import com.posprinter.printdemo.utils.UIUtils
import net.posprinter.POSPrinter

/**
 * @author: star
 * @date: 2022-10-15
 */
class ModifyNetDlg : AppCompatDialog {
    private val printer: POSPrinter
    private val bind: DlgModifyNetBinding
    private val screenWidth: Int
        get() {
            val wm = App.get().getSystemService(WINDOW_SERVICE) as WindowManager
            val point = Point()
            when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                true -> wm.defaultDisplay.getRealSize(point)
                false -> wm.defaultDisplay.getSize(point)
            }
            return point.x
        }

    constructor(context: Context, printer: POSPrinter) : super(context, R.style.dialog) {
        this.printer = printer
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(false)
        bind = DlgModifyNetBinding.inflate(layoutInflater)
        setContentView(bind.root)
        window?.let {
            val lp = it.attributes
            lp.width = screenWidth * 9 / 10
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            lp.dimAmount = 0.3f
            it.attributes = lp
            it.decorView.findViewById<View>(R.id.title).visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.cancelBtn.setOnClickListener {
            dismiss()
        }
        bind.confirmBtn.setOnClickListener {
            modify()
            dismiss()
        }
        bind.ipSetBtn.setOnClickListener {
            try {
                val ip = parseData(bind.ipEt.text.toString())
                printer.setIp(ip)
            } catch (e: Exception) {
                e.printStackTrace()
                UIUtils.toast(e.message!!)
            }
        }
        bind.maskSetBtn.setOnClickListener {
            try {
                val mask = parseData(bind.maskEt.text.toString())
                printer.setMask(mask)
            } catch (e: Exception) {
                e.printStackTrace()
                UIUtils.toast(e.message!!)
            }
        }
        bind.gatewayBtn.setOnClickListener {
            try {
                val gateway = parseData(bind.gatewayEt.text.toString())
                printer.setGateway(gateway)
            } catch (e: Exception) {
                e.printStackTrace()
                UIUtils.toast(e.message!!)
            }
        }
    }

    private fun modify() {
        try {
            val ip = parseData(bind.ipEt.text.toString())
            val mask = parseData(bind.maskEt.text.toString())
            val gateway = parseData(bind.gatewayEt.text.toString())
            val dhcp = bind.dhcpCb.isChecked
            printer.setNetAll(ip, mask, gateway, dhcp)
        } catch (e: Exception) {
            e.printStackTrace()
            UIUtils.toast(e.message!!)
        }
    }

    private fun parseData(str: String): ByteArray? {
        val arr = str.split(".")
        if (arr.size != 4) {
            return null
        }
        return byteArrayOf(arr[0].toInt().toByte(), arr[1].toInt().toByte(), arr[2].toInt().toByte(), arr[3].toInt().toByte())
    }

}