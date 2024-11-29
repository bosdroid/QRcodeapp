package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ROIAdapter(private val qrCodes: List<String>, private val roiList: List<Float>) :
    RecyclerView.Adapter<ROIAdapter.ROIViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ROIViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ROIViewHolder(view)
    }

    override fun onBindViewHolder(holder: ROIViewHolder, position: Int) {
        holder.bind(qrCodes[position], roiList[position])
    }

    override fun getItemCount(): Int = qrCodes.size

    class ROIViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(android.R.id.text1)
        private val roiValue: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(qrCode: String, roi: Float) {
            title.text = qrCode
            roiValue.text = "ROI: %.2f%%".format(roi)
        }
    }
}