package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.ItemQrTypeBinding
import com.expert.qrgenerator.model.QRTypes

class QRTypesAdapter(
    private val qrTypesList: List<QRTypes>,
    private val itemClickListener: (QRTypes,Int) -> Unit
) : RecyclerView.Adapter<QRTypesAdapter.QRTypesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QRTypesViewHolder {
        val binding = ItemQrTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QRTypesViewHolder(binding, itemClickListener)
    }

    override fun onBindViewHolder(holder: QRTypesViewHolder, position: Int) {
        val qrType = qrTypesList[position]
        holder.bind(qrType)
    }

    override fun getItemCount(): Int = qrTypesList.size

    class QRTypesViewHolder(
        private val binding: ItemQrTypeBinding,
        private val itemClickListener: (QRTypes,Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(qrType: QRTypes) {
            binding.itemImage.setImageResource(qrType.image)
            binding.itemText.text = qrType.name

            // Set click listener on the entire itemView
            itemView.setOnClickListener {
                itemClickListener(qrType,layoutPosition)
            }
        }
    }
}
