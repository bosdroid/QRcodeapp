package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.AddListValueItemLayoutBinding
import com.expert.qrgenerator.databinding.TableItemRowBinding
import com.expert.qrgenerator.model.ListItem

class FieldListAdapter(private val listValues: ArrayList<ListItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Interface for handling item click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    // Method to set the click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder for regular list items
    class ItemViewHolder(
        private val binding: TableItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the ViewHolder
        fun bindData(listItem: ListItem, position: Int) {
            binding.tableItemName.text = listItem.value
            itemView.setOnClickListener {
                mListener.onItemClick(position)
            }
        }
    }

    // ViewHolder for the "Add Item" button
    class AddItemViewHolder(
        private val binding: AddListValueItemLayoutBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the "Add Item" ViewHolder
        fun bindData(position: Int) {
            binding.addCardView.setOnClickListener {
                mListener.onAddItemClick(position)
            }
        }
    }

    // Inflates the appropriate ViewHolder based on the viewType
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ADD_ITEM) {
            val binding = AddListValueItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            AddItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        } else {
            val binding = TableItemRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        }
    }

    // Determines the view type based on the position
    override fun getItemViewType(position: Int): Int {
        return if (position == listValues.size) {
            VIEW_TYPE_ADD_ITEM
        } else {
            VIEW_TYPE_ITEM
        }
    }

    // Binds data to the appropriate ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_ADD_ITEM -> {
                (holder as AddItemViewHolder).bindData(position)
            }
            VIEW_TYPE_ITEM -> {
                (holder as ItemViewHolder).bindData(listValues[position], position)
            }
        }
    }

    // Returns the total number of items in the list including the "Add Item" button
    override fun getItemCount(): Int = listValues.size + 1

    companion object {
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_ADD_ITEM = 0
    }
}
