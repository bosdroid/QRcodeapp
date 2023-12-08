package com.expert.qrgenerator.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.AddColorItemRowBinding
import com.expert.qrgenerator.databinding.ColorItemRowBinding

class ColorAdapter(var context: Context, private var colorList: List<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null
    private var isIconUpdate: Boolean = false

    companion object {
        var selected_position = -1
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(
        private val binding: ColorItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        //        val colorBtn:AppCompatButton = itemView.findViewById(R.id.color_item)
//        val icon :AppCompatImageView = itemView.findViewById(R.id.selected_icon)
        fun bindData(color:String,position: Int,isIconUpdate:Boolean,adapter: ColorAdapter) {
            binding.colorItem.setBackgroundColor(Color.parseColor("#$color"))
            if (selected_position == position && isIconUpdate) {
                binding.selectedIcon.visibility = View.VISIBLE
            } else {
                binding.selectedIcon.visibility = View.INVISIBLE
            }

            binding.colorItem.setOnClickListener {

                val previousItem: Int = selected_position
                selected_position = position

                adapter.notifyItemChanged(previousItem)
                adapter.notifyItemChanged(position)

                mListener.onItemClick(position - 1)

            }
        }
    }

    class AddItemViewHolder(
        private val binding: AddColorItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(position: Int) {
         binding.addCardView.setOnClickListener {
             mListener.onAddItemClick(position)
         }
        }

    }

    fun updateAdapter(position: Int) {
        selected_position += 1
        notifyItemInserted(position)
        notifyDataSetChanged()
    }

    fun updateIcon(flag: Boolean) {
        isIconUpdate = flag
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val addColorItemRowBinding =
                AddColorItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            return AddItemViewHolder(addColorItemRowBinding, mListener!!)
        } else {
            val colorItemRowBinding =
                ColorItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ItemViewHolder(colorItemRowBinding, mListener!!)
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
        when (holder.itemViewType) {
            0 -> {
                val addViewHolder = holder as AddItemViewHolder
                addViewHolder.bindData(position)
            }
            else -> {
                val color = colorList[position - 1]
                val viewHolder = holder as ItemViewHolder
                viewHolder.bindData(color,position,isIconUpdate,this)

            }
        }

    }

}