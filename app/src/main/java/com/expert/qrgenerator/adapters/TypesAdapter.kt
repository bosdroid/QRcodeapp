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
import com.expert.qrgenerator.databinding.TypesItemRowBinding
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

    class ItemViewHolder(private val binding: TypesItemRowBinding,private val mListener: OnItemClickListener) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bindData(position: Int, type: QRTypes,context: Context,selected_position:Int,isEnableDisable:Boolean,adapter: TypesAdapter){
    binding.typeImage.setImageResource(type.image)
    binding.typeText.text = type.name

    if (isEnableDisable && position != 1)
    {
        binding.typesParentLayout.isEnabled = false
        binding.typesParentLayout.alpha = 0.8f
    }
    else
    {
        binding.typesParentLayout.isEnabled = true
        binding.typesParentLayout.alpha = 1.0f
    }

    if (selected_position == position) {
        binding.typesParentLayout.strokeColor = ContextCompat.getColor(context, R.color.black)
        binding.typesParentLayout.strokeWidth = 2
    } else {
        binding.typesParentLayout.strokeColor = ContextCompat.getColor(context, R.color.white)
        binding.typesParentLayout.strokeWidth = 0
    }

    binding.typesParentLayout.setOnClickListener {

        val previousItem: Int = selected_position
        adapter.updateSelectedPosition(position)

        adapter.notifyItemChanged(previousItem)
        adapter.notifyItemChanged(position)
        mListener.onItemClick(position)
    }

        }

    }

    // THIS FUNCTION WILL ENABLE AND DISABLE QR TYPES ON SWITCH CHANGE
    fun updatePosition(position: Int) {
        selected_position = position
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
         val typesItemRowBinding = TypesItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(typesItemRowBinding, mListener!!)

    }


    override fun getItemCount(): Int {
        return qrTypesList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val type = qrTypesList[position]
        holder.bindData(position,type,context, selected_position,isEnableDisable,this)

    }

    fun updateSelectedPosition(value:Int){
        selected_position = value
    }
}