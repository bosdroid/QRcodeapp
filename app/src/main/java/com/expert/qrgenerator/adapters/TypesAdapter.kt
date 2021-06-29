package com.expert.qrgenerator.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.QRTypes
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class TypesAdapter(val context: Context, private var qrTypesList: List<QRTypes>) :
    RecyclerView.Adapter<TypesAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    private var mListener: OnItemClickListener? = null
    private var isEnableDisable: Boolean = false

    companion object {
        var selected_position = 0
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(itemView: View, mListener: OnItemClickListener) : RecyclerView.ViewHolder(
        itemView
    ) {

        val parentLayout: MaterialCardView = itemView.findViewById(R.id.types_parent_layout)
        val typeImage: AppCompatImageView = itemView.findViewById(R.id.type_image)
        val typeText: MaterialTextView = itemView.findViewById(R.id.type_text)

    }

    // THIS FUNCTION WILL ENABLE AND DISABLE QR TYPES ON SWITCH CHANGE
    fun updatePosition(position: Int) {
        selected_position = position
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.types_item_row,
            parent,
            false
        )
        return ItemViewHolder(view, mListener!!)

    }


    override fun getItemCount(): Int {
        return qrTypesList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val type = qrTypesList[position]
        holder.typeImage.setImageResource(type.image)
        holder.typeText.text = type.name

        if (isEnableDisable && position != 1)
        {
            holder.parentLayout.isEnabled = false
            holder.parentLayout.alpha = 0.8f
        }
        else
        {
            holder.parentLayout.isEnabled = true
            holder.parentLayout.alpha = 1.0f
        }

        if (selected_position == position) {
            holder.parentLayout.strokeColor = ContextCompat.getColor(context, R.color.black)
            holder.parentLayout.strokeWidth = 2
        } else {
            holder.parentLayout.strokeColor = ContextCompat.getColor(context, R.color.white)
            holder.parentLayout.strokeWidth = 0
        }

        holder.parentLayout.setOnClickListener {

            val previousItem: Int = selected_position
            selected_position = position

            notifyItemChanged(previousItem)
            notifyItemChanged(position)
            mListener!!.onItemClick(position)
        }

    }
}