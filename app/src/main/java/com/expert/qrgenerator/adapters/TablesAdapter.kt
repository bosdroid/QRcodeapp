package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.AddTableItemLayoutBinding
import com.expert.qrgenerator.databinding.TableItemRowBinding
import com.expert.qrgenerator.utils.AppSettings
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import java.util.concurrent.TimeUnit

class TablesAdapter(
    private val context: Context,
    private val tableList: ArrayList<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Interface for item click events
    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null
    private var appSettings = AppSettings(context)
    private var addViewHolder: AddItemViewHolder? = null

    // Set listener for item click events
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    // ViewHolder for normal table items
    class ItemViewHolder(
        private val binding: TableItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(table: String) {
            binding.tableItemName.text = table
            itemView.setOnClickListener {
                mListener.onItemClick(adapterPosition)
            }
        }
    }

    // ViewHolder for the 'Add Item' button
    class AddItemViewHolder(
        private val binding: AddTableItemLayoutBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData() {
            binding.addNewTableBtn.setOnClickListener {
                mListener.onAddItemClick(adapterPosition)
            }
        }
    }

    // Create ViewHolder based on view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ADD_ITEM) {
            val binding = AddTableItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AddItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        } else {
            val binding = TableItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ItemViewHolder(binding, mListener ?: throw IllegalStateException("OnItemClickListener not set"))
        }
    }

    // Determine view type for each item
    override fun getItemViewType(position: Int): Int {
        return if (position == tableList.size) VIEW_TYPE_ADD_ITEM else VIEW_TYPE_TABLE_ITEM
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_ADD_ITEM -> {
                addViewHolder = holder as AddItemViewHolder
                addViewHolder?.bindData()
            }
            else -> {
                val table = tableList[position]
                val viewHolder = holder as ItemViewHolder
                // Show tooltip for the last item
                if (position == tableList.size - 1) {
                    showTooltipForLastItem(viewHolder)
                }
                viewHolder.bindData(table)
            }
        }
    }

    override fun getItemCount(): Int = tableList.size + 1

    // Show tooltip for the last table item
    private fun showTooltipForLastItem(holder: RecyclerView.ViewHolder) {
        if (appSettings.getBoolean(context.getString(R.string.key_tips))) {
            val lastShownTooltip = appSettings.getLong("tt11")
            if (lastShownTooltip == 0L || System.currentTimeMillis() - lastShownTooltip > TimeUnit.DAYS.toMillis(1)) {
                SimpleTooltip.Builder(context)
                    .anchorView(holder.itemView)
                    .text(context.getString(R.string.tt11_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt11", System.currentTimeMillis())
                        addViewHolder?.let { showTooltipForAddItem(it) }
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }

    // Show tooltip for the add item view
    private fun showTooltipForAddItem(holder: RecyclerView.ViewHolder) {
        if (appSettings.getBoolean(context.getString(R.string.key_tips))) {
            val lastShownTooltip = appSettings.getLong("tt12")
            if (lastShownTooltip == 0L || System.currentTimeMillis() - lastShownTooltip > TimeUnit.DAYS.toMillis(1)) {
                SimpleTooltip.Builder(context)
                    .anchorView(holder.itemView)
                    .text(context.getString(R.string.tt12_tip_text))
                    .gravity(Gravity.BOTTOM)
                    .animated(true)
                    .transparentOverlay(false)
                    .onDismissListener { tooltip ->
                        appSettings.putLong("tt12", System.currentTimeMillis())
                        tooltip.dismiss()
                    }
                    .build()
                    .show()
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_TABLE_ITEM = 1
        private const val VIEW_TYPE_ADD_ITEM = 0
    }
}
