package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
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

    class ItemViewHolder(itemView: View, Listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val sheetNameView: MaterialTextView = itemView.findViewById(R.id.sheet_item_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.sheet_item_row,
            parent,
            false
        )
        return ItemViewHolder(view, mListener!!)
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val item = sheetItems[position]
        holder.sheetNameView.text = item.name
        holder.itemView.setOnClickListener {
            mListener!!.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return sheetItems.size
    }

}