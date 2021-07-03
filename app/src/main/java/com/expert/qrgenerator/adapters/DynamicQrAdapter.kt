package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.QREntity
import com.google.android.material.textview.MaterialTextView

class DynamicQrAdapter(val context: Context,var dynamicQrList:ArrayList<CodeHistory>): RecyclerView.Adapter<DynamicQrAdapter.ItemViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemEditClick(position: Int)
        fun onItemClick(position: Int)
    }

    public fun setOnClickListener(mListener: OnItemClickListener) {
        listener = mListener
    }

    class ItemViewHolder(itemView:View, mListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView){

//        val qrIdView : MaterialTextView
//        val qrUrlView: MaterialTextView
        val qrBaseUrlView: MaterialTextView
        val qrEditView:AppCompatImageView

        init {
//            qrIdView = itemView.findViewById(R.id.dynamic_qr_id_item)
//            qrUrlView = itemView.findViewById(R.id.dynamic_qr_url_item)
            qrBaseUrlView = itemView.findViewById(R.id.dynamic_qr_baseUrl_item)
            qrEditView = itemView.findViewById(R.id.edit_dynamic_qr)
            itemView.setOnClickListener {
                mListener.onItemClick(layoutPosition)
            }
            qrEditView.setOnClickListener {
                mListener.onItemEditClick(layoutPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.dynamic_qr_single_item_row,parent,false)
        return ItemViewHolder(v,listener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val dynamicQr = dynamicQrList[position]

        //holder.qrIdView.text = dynamicQr.qrId
        holder.qrBaseUrlView.text = dynamicQr.data
        //holder.qrUrlView.text = dynamicQr.generatedUrl
    }

    override fun getItemCount(): Int {
        return dynamicQrList.size
    }


}