package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.ScanHistoryItemLayoutBinding
import com.expert.qrgenerator.model.TrackableScan
import com.expert.qrgenerator.ui.activities.BaseActivity

class TimestampAdapter(private val timestamps: List<TrackableScan>) :
    RecyclerView.Adapter<TimestampAdapter.TimestampViewHolder>() {

    class TimestampViewHolder(val binding: ScanHistoryItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(trackableScan: TrackableScan,position: Int) {
            val dateTimeString = BaseActivity.getDateTimeFromTimeStamp1(trackableScan.timestamp!! * 1000)
            binding.scanHistoryView.text = "${position+1}. ${dateTimeString}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimestampViewHolder {
        val binding = ScanHistoryItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimestampViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimestampViewHolder, position: Int) {
        val trackableScan = timestamps[position]
        holder.bind(trackableScan,position)
    }

    override fun getItemCount() = timestamps.size
}