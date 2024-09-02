package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.TypesItemRowBinding
import com.expert.qrgenerator.model.QRTypes

class TypesAdapter(
    private val context: Context,
    private val qrTypesList: List<QRTypes>
) : RecyclerView.Adapter<TypesAdapter.ItemViewHolder>() {

    // Listener interface for item clicks
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null
    private var isEnableDisable: Boolean = false
    private var selectedPosition = 0

    // Set the listener for item clicks
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder class for binding data to item views
    class ItemViewHolder(
        private val binding: TypesItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the view
        fun bindData(
            position: Int,
            type: QRTypes,
            context: Context,
            selectedPosition: Int,
            isEnableDisable: Boolean,
            adapter: TypesAdapter
        ) {
            binding.typeImage.setImageResource(type.image)
            binding.typeText.text = type.name

            // Enable/disable layout and adjust alpha based on isEnableDisable flag
            binding.typesParentLayout.isEnabled = !isEnableDisable || position == 1
            binding.typesParentLayout.alpha = if (isEnableDisable && position != 1) 0.8f else 1.0f

            // Highlight the selected item
            binding.typesParentLayout.strokeColor =
                ContextCompat.getColor(context, if (selectedPosition == position) R.color.black else R.color.white)
            binding.typesParentLayout.strokeWidth = if (selectedPosition == position) 2 else 0

            // Set click listener for the layout
            binding.typesParentLayout.setOnClickListener {
                val previousItem = selectedPosition
                adapter.updateSelectedPosition(position)
                adapter.notifyItemChanged(previousItem)
                adapter.notifyItemChanged(position)
                mListener.onItemClick(position)
            }
        }
    }

    // Update selected position and refresh the adapter
    fun updatePosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    // Create and return a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val typesItemRowBinding = TypesItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Ensure listener is set
        return ItemViewHolder(
            typesItemRowBinding,
            mListener ?: throw IllegalStateException("OnItemClickListener not set")
        )
    }

    // Return the number of items in the list
    override fun getItemCount(): Int = qrTypesList.size

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val type = qrTypesList[position]
        holder.bindData(position, type, context, selectedPosition, isEnableDisable, this)
    }

    // Update the selected position
    private fun updateSelectedPosition(value: Int) {
        selectedPosition = value
    }
}
