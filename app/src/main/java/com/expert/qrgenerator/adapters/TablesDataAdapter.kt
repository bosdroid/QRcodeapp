package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.Table
import com.google.android.material.textview.MaterialTextView

class TablesDataAdapter(val context: Context, val tableList: ArrayList<String>) :
    RecyclerView.Adapter<TablesDataAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, Listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val tableNameView: MaterialTextView
        init {
            tableNameView = itemView.findViewById(R.id.table_item_name)
            tableNameView.setOnClickListener{
                Listener.onItemClick(layoutPosition)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.table_item_row,
            parent,
            false
        )
        return ItemViewHolder(view, mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val table = tableList[position]
        holder.tableNameView.text = table


    }

    override fun getItemCount(): Int {
        return tableList.size
    }

}