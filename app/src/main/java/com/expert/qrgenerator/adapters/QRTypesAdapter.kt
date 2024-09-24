package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.HeaderLayoutBinding
import com.expert.qrgenerator.databinding.ItemQrTypeBinding
import com.expert.qrgenerator.model.QRItem

class QRTypesAdapter(
    private val qrTypesList: List<QRItem>,
    private val itemClickListener: (QRItem,Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val binding = HeaderLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemQrTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return QRTypesViewHolder(binding, itemClickListener)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val qrType = qrTypesList[position]) {
            is QRItem.Header -> {
                (holder as HeaderViewHolder).binding.headerTextView.text = qrType.title
            }
            is QRItem.QRType -> {
               val qrTypeHolder = (holder as QRTypesViewHolder)
                qrTypeHolder.bind(qrType)
            }
        }


    }

    override fun getItemCount(): Int = qrTypesList.size

    override fun getItemViewType(position: Int): Int {
        return when (qrTypesList[position]) {
            is QRItem.Header -> VIEW_TYPE_HEADER
            is QRItem.QRType -> VIEW_TYPE_ITEM
        }
    }

    // ViewHolder for Header using ViewBinding
    class HeaderViewHolder(val binding: HeaderLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    class QRTypesViewHolder(
        private val binding: ItemQrTypeBinding,
        private val itemClickListener: (QRItem,Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(qrType: QRItem.QRType) {
            binding.itemImage.setImageResource(qrType.image)
            binding.itemText.text = qrType.name
            if(layoutPosition == 2){
                binding.itemStarImage.visibility = View.VISIBLE
            }
            else{
                binding.itemStarImage.visibility = View.GONE
            }
            // Set click listener on the entire itemView
            itemView.setOnClickListener {
                itemClickListener(qrType,qrType.position)
            }
        }
    }
}
