package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.DynamicQrSingleItemRowBinding
import com.expert.qrgenerator.model.CodeHistory

class DynamicQrAdapter(private val dynamicQrList:ArrayList<CodeHistory>): RecyclerView.Adapter<DynamicQrAdapter.ItemViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemEditClick(position: Int)
        fun onItemClick(position: Int)
    }

    fun setOnClickListener(mListener: OnItemClickListener) {
        listener = mListener
    }

    class ItemViewHolder(private val binding:DynamicQrSingleItemRowBinding, private val mListener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root){

           fun bindData(codeHistory: CodeHistory){
              binding.dynamicQrBaseUrlItem.text = codeHistory.data
               itemView.setOnClickListener {
                   mListener.onItemClick(layoutPosition)
               }
             binding.editDynamicQr.setOnClickListener {
                 mListener.onItemEditClick(layoutPosition)
             }
           }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val dynamicQrSingleItemRowBinding = DynamicQrSingleItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ItemViewHolder(dynamicQrSingleItemRowBinding,listener?: throw IllegalStateException("OnItemClickListener not set"))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val dynamicQr = dynamicQrList[position]
        holder.bindData(dynamicQr)
    }

    override fun getItemCount(): Int = dynamicQrList.size



}