package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
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

    class ItemViewHolder(itemView: View, Listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val listValueView: MaterialTextView = itemView.findViewById(R.id.table_item_name)
    }

    class AddItemViewHolder(itemView: View, mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val addCardViewBtn: CardView = itemView.findViewById(R.id.add_card_view)
        val cardTextView:MaterialTextView = itemView.findViewById(R.id.card_text_view)
        val finishCardViewBtn :CardView = itemView.findViewById(R.id.finish_card_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.add_list_value_item_layout,
                parent,
                false
            )
            AddItemViewHolder(view, mListener!!)
        } else {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.table_item_row,
            parent,
            false
        )
        return ItemViewHolder(view, mListener!!)
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
                addViewHolder.cardTextView.text = context.resources.getString(R.string.add_value_text)
                addViewHolder.addCardViewBtn.setOnClickListener {
                    mListener!!.onAddItemClick(position)
                }
                addViewHolder.finishCardViewBtn.visibility = View.VISIBLE
                addViewHolder.finishCardViewBtn.setOnClickListener {
                    mListener!!.onFinishItemClick()
                }
            }
            else -> {
        val listValue = listValues[position]
                val viewHolder = holder as ItemViewHolder
                viewHolder.listValueView.text = listValue
                viewHolder.itemView.setOnClickListener {
            mListener!!.onItemClick(position)
        }
            }
        }
    }

    override fun getItemCount(): Int {
        return listValues.size+1
    }

}