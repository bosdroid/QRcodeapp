package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.SocialNetworksListItemDesignBinding
import com.expert.qrgenerator.model.SocialNetwork
import com.google.android.material.checkbox.MaterialCheckBox

class SocialNetworkAdapter(private val socialNetworkList:ArrayList<SocialNetwork>) : RecyclerView.Adapter<SocialNetworkAdapter.ItemViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(position: Int)
        fun onItemCheckClick(position: Int,isChecked: Boolean)
        fun onItemEditIconClick(position: Int,checkBox: MaterialCheckBox)
    }

    private var mListener: OnItemClickListener?=null

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.mListener = listener
    }

    open class ItemViewHolder(private val binding: SocialNetworksListItemDesignBinding,private val mListener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root){

        fun bindData(item:SocialNetwork){
            if (item.isActive == 0){
                itemView.alpha = 0.2f
                binding.snItemCheckbox.isChecked = false
            }
            else{
                itemView.alpha = 0.7f
                binding.snItemCheckbox.isChecked = true
            }

            binding.snItemLogo.setImageResource(item.icon)
            binding.snItemHeading.text = item.title
            binding.snItemTagline.text = item.url

            binding.snItemCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                mListener.onItemCheckClick(layoutPosition,isChecked)
            }

            binding.snItemEditIcon.setOnClickListener {

                mListener.onItemClick(layoutPosition)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val socialNetworksListItemDesignBinding = SocialNetworksListItemDesignBinding.inflate(
            LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(socialNetworksListItemDesignBinding,mListener?: throw IllegalStateException("OnItemClickListener not set"))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = socialNetworkList[position]
         holder.bindData(item)


    }

    override fun getItemCount(): Int = socialNetworkList.size

}