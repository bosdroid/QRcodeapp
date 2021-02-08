package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.expert.qrgenerator.R

class LogoAdapter(var context: Context, var logoList:List<String>) : RecyclerView.Adapter<LogoAdapter.ItemViewHolder>(){


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
        val image: AppCompatImageView
        val bgLayout: RelativeLayout
        val icon : AppCompatImageView

        init {
            image = itemView.findViewById(R.id.image_item)
            bgLayout = itemView.findViewById(R.id.parent_layout)
            icon = itemView.findViewById(R.id.image_selected_icon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.image_item_row,
            parent,
            false
        )
        return ItemViewHolder(view, mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val image = logoList[position]

        Glide.with(context).load(image).into(holder.image)
        if (selected_position == position)
        {
            holder.icon.visibility = View.VISIBLE
        }
        else
        {
            holder.icon.visibility = View.INVISIBLE
        }

        holder.image.setOnClickListener {
            val previousItem: Int = selected_position
            selected_position = position

            notifyItemChanged(previousItem)
            notifyItemChanged(position)

            mListener!!.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return logoList.size
    }


}