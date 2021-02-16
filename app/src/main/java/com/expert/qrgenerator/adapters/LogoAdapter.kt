package com.expert.qrgenerator.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.expert.qrgenerator.R

class LogoAdapter(var context: Context, private var logoList: List<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null
    private var isIconUpdate:Boolean = false

    companion object {
        var selected_position = -1
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, mListener: OnItemClickListener) : RecyclerView.ViewHolder(
        itemView
    ) {
        val image: AppCompatImageView = itemView.findViewById(R.id.image_item)
        val icon: AppCompatImageView = itemView.findViewById(R.id.image_selected_icon)

    }

    class AddItemViewHolder(itemView: View, mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val addCardViewBtn: CardView = itemView.findViewById(R.id.add_card_view)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.add_item_layout,
                parent,
                false
            )
            AddItemViewHolder(view, mListener!!)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.image_item_row,
                parent,
                false
            )
            ItemViewHolder(view, mListener!!)
        }
    }

    fun updateIcon(flag:Boolean)
    {
        isIconUpdate = flag
    }

    fun updateAdapter(position: Int){
        selected_position +=1
        notifyItemInserted(position)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        var viewType = 1 //Default Layout is 1
        if (position == 0) viewType = 0 //if zero, it will be a header view
        return viewType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder.itemViewType) {
            0 -> {
                val addViewHolder = holder as AddItemViewHolder
                addViewHolder.addCardViewBtn.setOnClickListener {
                    mListener!!.onAddItemClick(position)
                }
            }
            else -> {

                val image = logoList[position-1]
                val viewHolder = holder as ItemViewHolder

                if (image.contains("http") || image.contains("https"))
                {
                    Glide.with(context).load(image).into(viewHolder.image)
                }
                else
                {
                    val uri: Uri =  Uri.parse(image)
                    viewHolder.image.setImageURI(uri)
                }
                if (selected_position == position && isIconUpdate) {
                    viewHolder.icon.visibility = View.VISIBLE
                } else {
                    viewHolder.icon.visibility = View.INVISIBLE
                }

                viewHolder.image.setOnClickListener {
                    val previousItem: Int = selected_position
                    selected_position = position

                    notifyItemChanged(previousItem)
                    notifyItemChanged(position)

                    mListener!!.onItemClick(position-1)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return logoList.size
    }


}