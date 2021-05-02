package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.QREntity
import com.expert.qrgenerator.model.QRHistory
import com.expert.qrgenerator.view.activities.BaseActivity
import com.google.android.material.textview.MaterialTextView

class QrCodeHistoryAdapter(val context: Context, var qrCodeHistoryList:ArrayList<QRHistory>): RecyclerView.Adapter<QrCodeHistoryAdapter.ItemViewHolder>() {

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    public fun setOnClickListener(mListener: OnItemClickListener) {
        listener = mListener
    }

    class ItemViewHolder(itemView:View, mListener: OnItemClickListener) : RecyclerView.ViewHolder(itemView){

        val qrTypeIcon : AppCompatImageView
        val qrCodeDataView: MaterialTextView
        val qrCodeCreatedAtView: MaterialTextView

        init {
            qrTypeIcon = itemView.findViewById(R.id.qr_code_history_item_type_icon)
            qrCodeDataView = itemView.findViewById(R.id.qr_code_history_item_text)
            qrCodeCreatedAtView = itemView.findViewById(R.id.qr_code_history_item_created_date)

            itemView.setOnClickListener {
                mListener.onItemClick(layoutPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.qr_code_history_item_design,parent,false)
        return ItemViewHolder(v,listener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val qrHistory = qrCodeHistoryList[position]
        when(qrHistory.type){
            "text"->{
             holder.qrTypeIcon.setImageResource(R.drawable.ic_text)
            }
            "link"->{
                holder.qrTypeIcon.setImageResource(R.drawable.ic_link)
            }
            "contact"->{
                holder.qrTypeIcon.setImageResource(R.drawable.ic_person)
            }
            "wifi"->{
                holder.qrTypeIcon.setImageResource(R.drawable.ic_wifi)
            }
            "phone"->{
                holder.qrTypeIcon.setImageResource(R.drawable.ic_phone)
            }
            "sms"->{
                holder.qrTypeIcon.setImageResource(R.drawable.ic_sms)
            }
            "instagram"->{
                holder.qrTypeIcon.setImageResource(R.drawable.instagram)
            }
            "whatsapp"->{
                holder.qrTypeIcon.setImageResource(R.drawable.whatsapp)
            }
            "coupon"->{
                holder.qrTypeIcon.setImageResource(R.drawable.ic_coupon)
            }
            "feedback"->{
                holder.qrTypeIcon.setImageResource(R.drawable.ic_feedback)
            }
            else->{

            }
        }

        holder.qrCodeDataView.text = qrHistory.data
        holder.qrCodeCreatedAtView.text = BaseActivity.getFormattedDate(context,qrHistory.createdAt)
    }

    override fun getItemCount(): Int {
        return qrCodeHistoryList.size
    }


}