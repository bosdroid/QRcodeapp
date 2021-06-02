package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.TableObject
import com.google.android.material.textview.MaterialTextView

class TableDetailAdapter(val context: Context, var tableDetailList: ArrayList<TableObject>) :
    RecyclerView.Adapter<TableDetailAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val idTextView: MaterialTextView
        val codeDataTextView: MaterialTextView
        val dateTextView: MaterialTextView

        init {
            idTextView = itemView.findViewById(R.id.table_id_view)
            codeDataTextView = itemView.findViewById(R.id.table_code_data_view)
            dateTextView = itemView.findViewById(R.id.table_date_view)

            itemView.setOnClickListener {
                mListener.onItemClick(layoutPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.table_detail_row_design,
            parent,
            false
        )
        return ItemViewHolder(view, mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val tableObject = tableDetailList[position]
        holder.idTextView.text = "${tableObject.id}"
        holder.codeDataTextView.text = tableObject.code_data
        holder.dateTextView.text = tableObject.date
    }

    override fun getItemCount(): Int {
        return tableDetailList.size
    }

}