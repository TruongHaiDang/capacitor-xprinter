package com.posprinter.printdemo.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.posprinter.printdemo.databinding.ActivitySelectNetBinding
import com.posprinter.printdemo.widget.ModifyUdpNetDlg
import net.posprinter.esc.PosUdpNet
import net.posprinter.model.UdpDevice

/**
 * @author: star
 * @date: 2022-10-14
 */
class SelectNetActivity : AppCompatActivity() {
    private val datas = ArrayList<String>()
    private val devices = ArrayList<UdpDevice>()
    private val adapter: ArrayAdapter<String> by lazy {
        ArrayAdapter(this, android.R.layout.simple_list_item_1, datas)
    }
    private val bind by lazy {
        ActivitySelectNetBinding.inflate(layoutInflater)
    }
    private val posUdpNet = PosUdpNet()

    companion object {
        const val MAC_ADDRESS = "MAC_ADDRESS"
        const val OPT_MODIFY = 1
        const val OPT_SEARCH = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        bind.listView.adapter = adapter
        bind.listView.setOnItemClickListener { _, _, position, _ ->
            if (intent.getIntExtra("data", OPT_SEARCH) == OPT_SEARCH) {
                val intent = Intent()
                intent.putExtra(MAC_ADDRESS, devices[position].macStr)
                setResult(RESULT_OK, intent)
                finish()
            } else {
                ModifyUdpNetDlg(this, devices[position]).show()
            }
        }

        bind.refreshTv.setOnClickListener {
            datas.clear()
            devices.clear()
            adapter.notifyDataSetChanged()
            refresh()
        }
        refresh()
    }


    private fun refresh() {
        posUdpNet.searchNetDevice {
            if (!datas.contains(it.macStr)) {
                datas.add("${it.macStr} \\ ${it.ipStr}")
                devices.add(it)
                adapter.notifyDataSetChanged()
            }
        }
    }

}