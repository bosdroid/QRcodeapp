package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.AddListValueItemLayoutBinding
import com.expert.qrgenerator.databinding.TableItemRowBinding
import com.expert.qrgenerator.model.ListItem
import com.expert.qrgenerator.model.ListValue
import com.expert.qrgenerator.utils.AppSettings
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import java.util.concurrent.TimeUnit

class FieldListsAdapter(
    private val context: Context,
    private val listItems: ArrayList<ListItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Interface for item click callbacks
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    // Listener for handling item clicks
    private var mListener: OnItemClickListener? = null

    // Application settings instance
    private val appSettings = AppSettings(context)

    // Setter for the click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder for regular list items
    class ItemViewHolder(
        private val binding: TableItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the views
        fun bindData(listItem: ListItem, position: Int) {
            binding.tableItemName.text = listItem.value
            itemView.setOnClickListener {
                mListener.onItemClick(position)
            }
        }
    }

    // ViewHolder for the "Add Item" row
    class AddItemViewHolder(
        private val binding: AddListValueItemLayoutBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind data and set click listeners
        fun bindData(position: Int, adapter: FieldListsAdapter) {
            binding.addCardView.setOnClickListener {
                mListener.onAddItemClick(position)
            }
            adapter.openAddListTipsDialog(itemView)
        }
    }

    // Inflate the appropriate layout based on the view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ADD_ITEM) {
            val binding = AddListValueItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            AddItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        } else {
            val binding = TableItemRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        }
    }

    // Determine the view type based on the position
    override fun getItemViewType(position: Int): Int {
        return if (position == listItems.size) VIEW_TYPE_ADD_ITEM else VIEW_TYPE_LIST_ITEM
    }

    // Bind data to the appropriate ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_ADD_ITEM -> (holder as AddItemViewHolder).bindData(position, this)
            VIEW_TYPE_LIST_ITEM -> (holder as ItemViewHolder).bindData(listItems[position], position)
        }
    }

    // Return the total item count, including the "Add Item" row
    override fun getItemCount(): Int = listItems.size + 1

    // Show a tooltip if conditions are met
    private fun openAddListTipsDialog(itemView: View) {
        if (appSettings.getBoolean(context.getString(R.string.key_tips))) {
            val lastShownTime = appSettings.getLong("tt22")
            if (lastShownTime == 0L || System.currentTimeMillis() - lastShownTime > TimeUnit.DAYS.toMillis(1)) {
                SimpleTooltip.Builder(context)
                    .anchorView(itemView)
                    .text(context.getString(R.string.tt22_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener {
                        appSettings.putLong("tt22", System.currentTimeMillis())
                    }
                    .build()
                    .show()
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_ADD_ITEM = 0
        private const val VIEW_TYPE_LIST_ITEM = 1
    }
}
