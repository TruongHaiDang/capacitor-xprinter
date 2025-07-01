package com.posprinter.printdemo.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.posprinter.printdemo.databinding.ItemSelectBluetoothBinding

/**
 * @author: star.
 *
 * @date: 2022-06-15
 */
class BtAdapter(private val datas: MutableList<Bean>) : RecyclerView.Adapter<BtAdapter.RvHolder>() {
    var itemClick: ((position: Int) -> Unit)? = null

    class RvHolder(val bind: ItemSelectBluetoothBinding) : RecyclerView.ViewHolder(bind.root) {

    }

    data class Bean(
        var isMatching: Boolean,
        var name: String,
        var mac: String
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvHolder {
        val inflate = ItemSelectBluetoothBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = RvHolder(inflate)
        inflate.root.setOnClickListener {
            itemClick?.invoke(holder.adapterPosition)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RvHolder, position: Int) {
        val data = datas[holder.adapterPosition]
        val bind = holder.bind
        bind.nameTv.text = data.name
        bind.macTv.text = data.mac
        bind.connectedTv.visibility = if (data.isMatching) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = datas.size
}