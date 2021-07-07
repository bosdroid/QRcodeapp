package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
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

    inner class ItemViewHolder(itemView: View, mListener: OnItemClickListener):RecyclerView.ViewHolder(itemView){
        val commentView:MaterialTextView
        val ratingBar:AppCompatRatingBar

        init {
            commentView = itemView.findViewById(R.id.feedback_item_comment)
             ratingBar = itemView.findViewById(R.id.feedback_item_stars)
            itemView.setOnClickListener {
                mListener.onItemClick(layoutPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.feedback_item_row,
            parent,
            false
        )
        return ItemViewHolder(v,mListener!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = feedbackList[position]
        holder.commentView.text = item.comment
        holder.ratingBar.rating = item.rating.toFloat()
    }



    override fun getItemCount(): Int {
        return feedbackList.size
    }

}