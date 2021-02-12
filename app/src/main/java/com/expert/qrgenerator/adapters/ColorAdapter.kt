package com.expert.qrgenerator.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R

class ColorAdapter(var context: Context, var colorList: List<String>) :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    interface OnItemClickListener{
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }
    var mListener: OnItemClickListener?=null
    var mContext = context
    var isIconUpdate:Boolean = false

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

    class AddItemViewHolder(itemView: View, mListener: OnItemClickListener):RecyclerView.ViewHolder(itemView){
         val addCardViewBtn:CardView

         init {
             addCardViewBtn = itemView.findViewById(R.id.add_card_view)
         }
    }

    fun updateAdapter(position: Int){
        selected_position +=1
        notifyItemInserted(position)
        notifyDataSetChanged()
    }

    fun updateIcon(flag:Boolean)
    {
        isIconUpdate = flag
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0)
        {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.add_color_item_row,
                parent,
                false
            )
            return AddItemViewHolder(view, mListener!!)
        }
        else
        {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.color_item_row,
                parent,
                false
            )
            return ItemViewHolder(view, mListener!!)
        }

    }

    override fun getItemViewType(position: Int): Int {
        var viewType = 1 //Default Layout is 1
        if (position == 0) viewType = 0 //if zero, it will be a header view
        return viewType
    }

    override fun getItemCount(): Int {
        return colorList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            0 -> {
                val addViewHolder = holder as AddItemViewHolder
                addViewHolder.addCardViewBtn.setOnClickListener {
                    mListener!!.onAddItemClick(position)
                }
            }
            else->{
                val color = colorList[position-1]
                val viewHolder = holder as ItemViewHolder
                viewHolder.colorBtn.setBackgroundColor(Color.parseColor("#$color"))
                if (selected_position == position && isIconUpdate)
                {
                    viewHolder.icon.visibility = View.VISIBLE
                }
                else
                {
                    viewHolder.icon.visibility = View.INVISIBLE
                }

                viewHolder.colorBtn.setOnClickListener {

                    val previousItem: Int = selected_position
                    selected_position = position

                    notifyItemChanged(previousItem)
                    notifyItemChanged(position)

                    mListener!!.onItemClick(position-1)

                }
            }
        }

    }

}