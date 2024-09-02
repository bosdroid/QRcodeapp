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

class FeedbackAdapter(
    private val feedbackList: ArrayList<Feedback> // The list of feedback items to display
) : RecyclerView.Adapter<FeedbackAdapter.ItemViewHolder>() {

    // Interface to handle item click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    // Listener for handling item clicks
    private var mListener: OnItemClickListener? = null

    // Method to set the click listener from outside the adapter
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder class for holding the view for each feedback item
    inner class ItemViewHolder(
        private val binding: FeedbackItemRowBinding, // View binding for the feedback item
        private val mListener: OnItemClickListener? // Listener passed to the ViewHolder
    ) : RecyclerView.ViewHolder(binding.root) {

        // Binds the data from the feedback item to the UI components
        fun bindData(feedback: Feedback) {
            binding.feedbackItemComment.text = feedback.comment // Set the comment text
            binding.feedbackItemStars.rating = feedback.rating.toFloat() // Set the star rating
            itemView.setOnClickListener {
                mListener?.onItemClick(layoutPosition) // Trigger the click listener with the item's position
            }
        }
    }

    // Inflates the layout and creates a ViewHolder for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FeedbackItemRowBinding.inflate(inflater, parent, false)

        return ItemViewHolder(
            binding,
            mListener ?: throw IllegalStateException("OnItemClickListener not set") // Ensure the listener is set
        )
    }

    // Binds the data to the ViewHolder for the given position
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = feedbackList[position]
        holder.bindData(item)
    }

    // Returns the total number of items in the list
    override fun getItemCount(): Int = feedbackList.size
}
