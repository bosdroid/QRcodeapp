package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FeedbackItemRowBinding
import com.expert.qrgenerator.model.Feedback
import com.google.android.material.textview.MaterialTextView

class FeedbackAdapter(val context: Context, val feedbackList: ArrayList<Feedback>):RecyclerView.Adapter<FeedbackAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    inner class ItemViewHolder(private val binding:FeedbackItemRowBinding,private val mListener: OnItemClickListener):RecyclerView.ViewHolder(binding.root){
        fun bindData(feedback: Feedback,position: Int){
            binding.feedbackItemComment.text = feedback.comment
            binding.feedbackItemStars.rating = feedback.rating.toFloat()
            itemView.setOnClickListener {
                mListener.onItemClick(layoutPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val feedbackItemRowBinding = FeedbackItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(feedbackItemRowBinding,mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = feedbackList[position]
        holder.bindData(item,position)

    }



    override fun getItemCount(): Int {
        return feedbackList.size
    }

}