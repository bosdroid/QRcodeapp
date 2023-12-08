package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.SheetItemRowBinding
import com.expert.qrgenerator.model.Sheet
import com.google.android.material.textview.MaterialTextView

class SheetAdapter(val context: Context, val sheetItems: ArrayList<Sheet>) :
    RecyclerView.Adapter<SheetAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(private val binding:SheetItemRowBinding,private val mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

          fun bindData(sheet: Sheet){
                    binding.sheetItemName.text = sheet.name
                    itemView.setOnClickListener {
                        mListener.onItemClick(layoutPosition)
                    }
          }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
       val sheetItemRowBinding = SheetItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(sheetItemRowBinding, mListener!!)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val item = sheetItems[position]
        holder.bindData(item)
    }

    override fun getItemCount(): Int {
        return sheetItems.size
    }

}