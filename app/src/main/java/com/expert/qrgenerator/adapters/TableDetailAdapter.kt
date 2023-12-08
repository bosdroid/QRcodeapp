package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.TableDetailRowDesignBinding
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

    class ItemViewHolder(private val binding:TableDetailRowDesignBinding,private val mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(tableObject: TableObject){
            binding.tableIdView.text = "${tableObject.id}"
            binding.tableCodeDataView.text = tableObject.code_data
            binding.tableDateView.text = tableObject.date

            itemView.setOnClickListener {
                mListener.onItemClick(layoutPosition)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val tableDetailRowDesignBinding = TableDetailRowDesignBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(tableDetailRowDesignBinding, mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val tableObject = tableDetailList[position]
        holder.bindData(tableObject)

    }

    override fun getItemCount(): Int {
        return tableDetailList.size
    }

}