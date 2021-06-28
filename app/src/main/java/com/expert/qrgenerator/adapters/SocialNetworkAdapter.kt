package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.SocialNetwork
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView

class SocialNetworkAdapter(val context: Context, var socialNetworkList:ArrayList<SocialNetwork>) : RecyclerView.Adapter<SocialNetworkAdapter.ItemViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(position: Int)
        fun onItemCheckClick(position: Int,isChecked: Boolean)
        fun onItemEditIconClick(position: Int,checkBox: MaterialCheckBox)
    }

    private var mListener: OnItemClickListener?=null

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.mListener = listener
    }

    open class ItemViewHolder(itemView:View, mListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView){
        val iconImageView:AppCompatImageView
        val titleTextView:MaterialTextView
        val descriptionTextView:MaterialTextView
        val checkBox:MaterialCheckBox
        val editIconLayout:LinearLayout

        init {
            iconImageView = itemView.findViewById(R.id.sn_item_logo)
            titleTextView = itemView.findViewById(R.id.sn_item_heading)
            descriptionTextView = itemView.findViewById(R.id.sn_item_tagline)
            checkBox = itemView.findViewById(R.id.sn_item_checkbox)
            editIconLayout = itemView.findViewById(R.id.sn_item_edit_icon)

            itemView.setOnClickListener {
                //mListener.onItemClick(layoutPosition)
            }
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                mListener.onItemCheckClick(layoutPosition,isChecked)
            }

            editIconLayout.setOnClickListener {
                //mListener.onItemEditIconClick(layoutPosition,checkBox)
                mListener.onItemClick(layoutPosition)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.social_networks_list_item_design,parent,false)
        return ItemViewHolder(v,mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = socialNetworkList[position]

        if (item.isActive == 0){
            holder.itemView.alpha = 0.2f
            holder.checkBox.isChecked = false
        }
        else{
            holder.itemView.alpha = 0.7f
            holder.checkBox.isChecked = true
        }

        holder.iconImageView.setImageResource(item.icon)
        holder.titleTextView.text = item.title
        holder.descriptionTextView.text = item.url

    }

    override fun getItemCount(): Int {
        return socialNetworkList.size
    }

}