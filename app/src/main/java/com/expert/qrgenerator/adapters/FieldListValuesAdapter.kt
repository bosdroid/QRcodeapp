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
import com.google.android.material.textview.MaterialTextView

class FieldListValuesAdapter(val context: Context, val listValues: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
        fun onFinishItemClick()
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(private val binding: TableItemRowBinding,private val mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

            fun bindData(value:String,position: Int){
                    binding.tableItemName.text = value
                    itemView.setOnClickListener {
                        mListener.onItemClick(position)
                    }
            }
    }

    class AddItemViewHolder(private val binding: AddListValueItemLayoutBinding,private val mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

          fun bindData(position: Int,context: Context){
                            binding.cardTextView.text = context.resources.getString(R.string.add_value_text)
                            binding.addCardView.setOnClickListener {
                                mListener.onAddItemClick(position)
                            }
                            binding.finishCardView.visibility = View.VISIBLE
                            binding.finishCardView.setOnClickListener {
                                mListener.onFinishItemClick()
                            }
          }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val addListValueItemLayoutBinding = AddListValueItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)

            AddItemViewHolder(addListValueItemLayoutBinding, mListener!!)
        } else {
        val tableItemRowBinding = TableItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(tableItemRowBinding, mListener!!)
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
                addViewHolder.bindData(position,context)

            }
            else -> {
        val listValue = listValues[position]
                val viewHolder = holder as ItemViewHolder
                viewHolder.bindData(listValue, position)

            }
        }
    }

    override fun getItemCount(): Int {
        return listValues.size+1
    }

}