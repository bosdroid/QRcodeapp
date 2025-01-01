package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.HeaderLayoutBinding
import com.expert.qrgenerator.databinding.ItemQrTypeBinding
import com.expert.qrgenerator.model.QRItem
import com.expert.qrgenerator.ui.fragments.ChooseTypeFragment
import com.expert.qrgenerator.utils.Constants

class QRTypesAdapter(
    private val qrTypesList: List<QRItem>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
    }

    interface OnItemClickListener{
        fun itemClickListener(type:String,position: Int)
        fun itemIconClickListener(position: Int)
    }

    private var listener:OnItemClickListener?=null

    fun setItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val binding = HeaderLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemQrTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return QRTypesViewHolder(binding, listener!!)
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
        private val listener: OnItemClickListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(qrType: QRItem.QRType) {
            binding.itemImage.setImageResource(qrType.image)
            binding.itemText.text = qrType.name
            if(layoutPosition == 0 || layoutPosition == 1){
//                binding.itemStarImage.visibility = View.VISIBLE
                if (layoutPosition == 0){
                    ChooseTypeFragment.infoImageView3 = binding.itemStarImage
                }
                else{
                    ChooseTypeFragment.infoImageView4 = binding.itemStarImage
                }
//                Constants.startShakeAnimation(binding.itemStarImage)
            }
            else{
                binding.itemStarImage.visibility = View.GONE
            }
            // Set click listener on the entire itemView
            itemView.setOnClickListener {
                listener.itemClickListener(qrType.name,qrType.position)
            }

            binding.itemStarImage.setOnClickListener {
//                if (layoutPosition == 0){
//                    TooltipCompat.setTooltipText(binding.itemStarImage, binding.itemStarImage.context.getString(R.string.vcard_hint_message))
//                    binding.itemStarImage.performLongClick()
                    listener.itemIconClickListener(layoutPosition)
//                }
//                else if (layoutPosition == 1){
////                    TooltipCompat.setTooltipText(binding.itemStarImage, binding.itemStarImage.context.getString(R.string.dynamic_link_hint_message1))
////                    binding.itemStarImage.performLongClick()
//                    listener.itemIconClickListener(qrType.position)
//                }
            }
        }
    }
}
