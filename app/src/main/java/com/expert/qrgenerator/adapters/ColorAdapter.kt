package com.expert.qrgenerator.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R

class ColorAdapter(var context: Context, var colorList: List<String>) :RecyclerView.Adapter<ColorAdapter.ItemViewHolder>(){

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
    var mListener: OnItemClickListener?=null
    var mContext = context
    companion object{
        var selected_position = -1
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, mListener: OnItemClickListener) : RecyclerView.ViewHolder(
        itemView
    )
    {
        val colorBtn:AppCompatButton
        val bgLayout:RelativeLayout
        val icon :AppCompatImageView

        init {
            colorBtn = itemView.findViewById(R.id.color_item)
            bgLayout = itemView.findViewById(R.id.parent_layout)
            icon = itemView.findViewById(R.id.selected_icon)
//            colorBtn.setOnClickListener {
//
//                mListener.onItemClick(layoutPosition)
//
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.color_item_row,
            parent,
            false
        )
        return ItemViewHolder(view, mListener!!)
    }

    override fun getItemCount(): Int {
        return colorList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val color = colorList[position]
        holder.colorBtn.setBackgroundColor(Color.parseColor("#$color"))
        if (selected_position == position)
        {
            holder.icon.visibility = View.VISIBLE
        }
        else
        {
            holder.icon.visibility = View.INVISIBLE
        }

        holder.colorBtn.setOnClickListener {
            val previousItem: Int = selected_position
            selected_position = position

            notifyItemChanged(previousItem)
            notifyItemChanged(position)

            mListener!!.onItemClick(position)
        }

    }

}