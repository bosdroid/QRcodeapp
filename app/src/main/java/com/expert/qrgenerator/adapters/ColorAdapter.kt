package com.expert.qrgenerator.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.AddColorItemRowBinding
import com.expert.qrgenerator.databinding.ColorItemRowBinding

class ColorAdapter(private val colorList: List<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null
    private var isIconUpdate: Boolean = false
    private var selected_position = -1

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(
        private val binding: ColorItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bindData(color:String,position: Int,isIconUpdate:Boolean,adapter: ColorAdapter,selected_position:Int) {
            binding.colorItem.setBackgroundColor(Color.parseColor("#$color"))
            if (selected_position == position && isIconUpdate) {
                binding.selectedIcon.visibility = View.VISIBLE
            } else {
                binding.selectedIcon.visibility = View.INVISIBLE
            }

            binding.colorItem.setOnClickListener {

                val previousItem: Int = selected_position

                adapter.updateSelectedPosition(position)
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

    private fun updateSelectedPosition(newPosition: Int){
        selected_position = newPosition
        notifyDataSetChanged()
    }


    // this function update the position of selected color box
    fun updateAdapter(position: Int) {
        selected_position += 1
        notifyItemInserted(position)
        notifyDataSetChanged()
    }

    // this function is public because it is used in activity for showing select tick icon
    fun updateIcon(flag: Boolean) {
        isIconUpdate = flag
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val addColorItemRowBinding =
                AddColorItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)

             AddItemViewHolder(addColorItemRowBinding, mListener?: throw IllegalStateException("OnItemClickListener not set"))
        } else {
            val colorItemRowBinding =
                ColorItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
             ItemViewHolder(colorItemRowBinding, mListener?: throw IllegalStateException("OnItemClickListener not set"))
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1
    }

    override fun getItemCount(): Int = colorList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                val addViewHolder = holder as AddItemViewHolder
                addViewHolder.bindData(position)
            }
            else -> {
                val color = colorList[position - 1]
                val viewHolder = holder as ItemViewHolder
                viewHolder.bindData(color,position,isIconUpdate,this,selected_position)

            }
        }

    }

}