package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.CategoryItemDesignBinding
import com.expert.qrgenerator.databinding.TagItemDesignBinding
import com.expert.qrgenerator.model.Folder
import com.expert.qrgenerator.model.FolderWithCount
import java.util.Locale

class CategoriesAdapter(
    private val context: Context,
    private val categoriesList: List<FolderWithCount>
) : RecyclerView.Adapter<CategoriesAdapter.ItemViewHolder>() {

    // Interface for handling item click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    // Listener for click events
    private var mListener: OnItemClickListener? = null

    // Method to set the item click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }


    // ViewHolder class to hold and bind item views
    class ItemViewHolder(
        private val binding: CategoryItemDesignBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Method to bind data to the item view
        fun bindData(
            position: Int,
            folder:FolderWithCount
        ) {
            binding.cateItemView.text = folder.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }
            binding.cateItemCountView.text = "${folder.codeHistoryCount} Items"

            binding.root.setOnClickListener {
                mListener.onItemClick(position)
            }
        }
    }

    // Inflates the item view and creates a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = CategoryItemDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
    }

    // Binds data to the ViewHolder for a given position
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val folder = categoriesList[position]
        holder.bindData(position, folder)
    }

    // Returns the total count of items
    override fun getItemCount(): Int = categoriesList.size
}
