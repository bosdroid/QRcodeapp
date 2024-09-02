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

class FieldListValuesAdapter(
    private val context: Context,
    private val listValues: ArrayList<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Interface to handle item click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
        fun onFinishItemClick()
    }

    // Listener for handling item clicks
    private var mListener: OnItemClickListener? = null

    // Method to set the item click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder class for regular list items
    class ItemViewHolder(
        private val binding: TableItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the view holder
        fun bindData(value: String, position: Int) {
            binding.tableItemName.text = value
            itemView.setOnClickListener {
                mListener.onItemClick(position)
            }
        }
    }

    // ViewHolder class for the "Add Item" layout
    class AddItemViewHolder(
        private val binding: AddListValueItemLayoutBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the add item view holder
        fun bindData(position: Int, context: Context) {
            binding.cardTextView.text = context.getString(R.string.add_value_text)
            binding.addCardView.setOnClickListener {
                mListener.onAddItemClick(position)
            }
            binding.finishCardView.visibility = View.VISIBLE
            binding.finishCardView.setOnClickListener {
                mListener.onFinishItemClick()
            }
        }
    }

    // Create appropriate ViewHolder based on the item type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {  // Add item view type
            val binding = AddListValueItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            AddItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        } else {  // Regular item view type
            val binding = TableItemRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        }
    }

    // Determine the type of view for the item at a given position
    override fun getItemViewType(position: Int): Int {
        return if (position == listValues.size) 0 else 1
    }

    // Bind data to the appropriate ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == 0) {
            (holder as AddItemViewHolder).bindData(position, context)
        } else {
            (holder as ItemViewHolder).bindData(listValues[position], position)
        }
    }

    // Return the total item count including the add item layout
    override fun getItemCount(): Int = listValues.size + 1
}
