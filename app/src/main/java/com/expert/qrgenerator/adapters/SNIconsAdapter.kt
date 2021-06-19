package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R

class SNIconsAdapter(var context: Context,var iconsList:ArrayList<Pair<String,Int>>):RecyclerView.Adapter<SNIconsAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(pos: Int)
    }

    private var mListener: OnItemClickListener?=null

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.mListener = listener
    }

    inner class ItemViewHolder(itemView:View, mListener: OnItemClickListener):RecyclerView.ViewHolder(itemView){
         val iconImage:AppCompatImageView

         init {
             iconImage = itemView.findViewById(R.id.sn_icon_item_view)

             iconImage.setOnClickListener {
                 mListener.onItemClick(layoutPosition)
             }
         }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.sn_icon_item_row_design,parent,false)
        return ItemViewHolder(v,mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val pair = iconsList[position]
        holder.iconImage.setImageResource(pair.second)
    }

    override fun getItemCount(): Int {
        return iconsList.size
    }

}