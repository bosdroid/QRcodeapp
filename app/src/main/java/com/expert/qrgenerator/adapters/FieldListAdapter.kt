package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.AddListValueItemLayoutBinding
import com.expert.qrgenerator.databinding.TableItemRowBinding
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.model.ListValue
import com.google.android.material.textview.MaterialTextView

class FieldListAdapter(val context: Context, val listValues: ArrayList<ListItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(private val binding:TableItemRowBinding, private val mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {
            fun bindData(listItem: ListItem,position: Int){
                binding.tableItemName.text = listItem.value
                itemView.setOnClickListener {
                    mListener.onItemClick(position)
                }
            }

    }

    class AddItemViewHolder(private val binding:AddListValueItemLayoutBinding, private val mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {
            fun bindData(position: Int){
                binding.addCardView.setOnClickListener {
                    mListener.onAddItemClick(position)
                }
            }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val addListValueItemLayoutBinding = AddListValueItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)

            AddItemViewHolder(addListValueItemLayoutBinding, mListener!!)
        } else {
            val tableItemRowBinding = TableItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

            ItemViewHolder(tableItemRowBinding, mListener!!)
        }
    }


    override fun getItemViewType(position: Int): Int {
        var viewType = 1 //Default Layout is 1
        if (position == listValues.size) viewType = 0 //if zero, it will be a header view
        return viewType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                val addViewHolder = holder as AddItemViewHolder
                addViewHolder.bindData(position)

            }
            else -> {
                val listValue = listValues[position]
                val viewHolder = holder as ItemViewHolder
                viewHolder.bindData(listValue,position)

            }
        }
    }

    override fun getItemCount(): Int {
        return listValues.size+1
    }

}