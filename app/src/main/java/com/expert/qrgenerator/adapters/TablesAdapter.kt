package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.Table
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class TablesAdapter(val context: Context, val tableList: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, Listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val tableNameView: MaterialTextView = itemView.findViewById(R.id.table_item_name)
    }

    class AddItemViewHolder(itemView: View, mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val addNewTableButton = itemView.findViewById<MaterialButton>(R.id.add_new_table_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.add_table_item_layout,
                parent,
                false
            )
            AddItemViewHolder(view, mListener!!)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.table_item_row,
                parent,
                false
            )
            ItemViewHolder(view, mListener!!)
        }
    }


    override fun getItemViewType(position: Int): Int {
        var viewType = 1 //Default Layout is 1
        if (position == tableList.size) viewType = 0 //if zero, it will be a header view
        return viewType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                val addViewHolder = holder as AddItemViewHolder
                addViewHolder.addNewTableButton.setOnClickListener {
                    mListener!!.onAddItemClick(position)
                }
            }
            else -> {
                val table = tableList[position]
                val viewHolder = holder as ItemViewHolder
                viewHolder.tableNameView.text = table
                viewHolder.itemView.setOnClickListener {
                    mListener!!.onItemClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
            return tableList.size+1
    }

}