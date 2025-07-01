package com.posprinter.printdemo.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.posprinter.printdemo.R

/**
 * @author: star
 * @date: 2022-04-21
 */
class DlgUsbSelect(
    private val activity: Activity,
    private val usbList: List<String>,
    private val callBack: ((str: String) -> Unit)
) {
    private lateinit var lvUsb: ListView

    @SuppressLint("SetTextI18n")
    fun show() {
        val inflater = LayoutInflater.from(activity)
        val dialogView3 = inflater.inflate(R.layout.usb_link, null)
        val tvUsb = dialogView3.findViewById(R.id.textView1) as TextView
        lvUsb = dialogView3.findViewById(R.id.listView1) as ListView
        tvUsb.text = activity.getString(R.string.usb_pre_con) + usbList.size
        val adapter3 = ArrayAdapter(activity, android.R.layout.simple_list_item_1, usbList)
        lvUsb.adapter = adapter3
        val dialog: AlertDialog = AlertDialog.Builder(activity)
            .setView(dialogView3).create()
        dialog.show()
        setUsbListener(dialog)
    }


    private fun setUsbListener(dialog: AlertDialog) {
        lvUsb.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, i, _ ->
                callBack.invoke(usbList[i])
                dialog.dismiss()
            }
    }

}