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

class FieldListsAdapter(val context: Context, val listItems: ArrayList<ListItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null
    private var appSettings = AppSettings(context)

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(private val binding: TableItemRowBinding,private val mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

            fun bindData(listItem: ListItem, position: Int){
                        binding.tableItemName.text = listItem.value
                        itemView.setOnClickListener {
                            mListener.onItemClick(position)
                        }
            }
    }

    class AddItemViewHolder(private val binding:AddListValueItemLayoutBinding,private val mListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

          fun bindData(position: Int,appSettings: AppSettings,context: Context){
                binding.addCardView.setOnClickListener {
                    mListener.onAddItemClick(position)
                }
                openAddListTipsDialog(itemView,appSettings,context)
          }

        private fun openAddListTipsDialog(itemView: View,appSettings: AppSettings,context: Context) {
            if (appSettings.getBoolean(context.resources.getString(R.string.key_tips))) {
                val duration = appSettings.getLong("tt22")
                if (duration.compareTo(0) == 0 || System.currentTimeMillis()-duration > TimeUnit.DAYS.toMillis(1) ) {
                    SimpleTooltip.Builder(context)
                        .anchorView(itemView)
                        .text(context.resources.getString(R.string.tt22_tip_text))
                        .gravity(Gravity.BOTTOM)
                        .animated(true)
                        .transparentOverlay(false)
                        .onDismissListener { tooltip ->
                            appSettings.putLong("tt22",System.currentTimeMillis())
                            tooltip.dismiss()
                        }
                        .build()
                        .show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val addListValueItemLayoutBinding = AddListValueItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)

            AddItemViewHolder(addListValueItemLayoutBinding, mListener!!)
        } else {
        val tableItemRowBinding = TableItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(tableItemRowBinding, mListener!!)
        }
    }


    override fun getItemViewType(position: Int): Int {
        var viewType = 1 //Default Layout is 1
        if (position == listItems.size) viewType = 0 //if zero, it will be a header view
        return viewType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                val addViewHolder = holder as AddItemViewHolder
                addViewHolder.bindData(position,appSettings,context)

            }
            else -> {
               val listValue = listItems[position]
                val viewHolder = holder as ItemViewHolder
                viewHolder.bindData(listValue,position)

            }
        }
    }

    override fun getItemCount(): Int {
        return listItems.size+1
    }



}