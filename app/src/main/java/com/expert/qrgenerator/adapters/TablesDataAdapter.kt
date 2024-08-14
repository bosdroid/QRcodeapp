package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.TableItemRowBinding

class TablesDataAdapter(private val tableList: ArrayList<String>) :
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

        return ItemViewHolder(tableItemRowBinding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val table = tableList[position]
        holder.bindData(table)

    }

    override fun getItemCount(): Int = tableList.size

}