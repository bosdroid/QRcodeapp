package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.TableItemRowBinding
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

    class ItemViewHolder(private val binding:TableItemRowBinding,private val mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {
            fun bindData(table:String){
                binding.tableItemName.text = table
            }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val tableItemRowBinding = TableItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(tableItemRowBinding, mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val table = tableList[position]
        holder.bindData(table)

    }

    override fun getItemCount(): Int {
        return tableList.size
    }

}