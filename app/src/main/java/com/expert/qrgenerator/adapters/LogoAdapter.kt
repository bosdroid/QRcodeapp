package com.expert.qrgenerator.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.AddItemLayoutBinding
import com.expert.qrgenerator.databinding.ImageItemRowBinding

class LogoAdapter(private val context: Context, private val logoList: List<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAddItemClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null
    private var isIconUpdate: Boolean = false
    private var selected_position = -1

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mListener = listener
    }

    class ItemViewHolder(
        private val binding: ImageItemRowBinding,
        private val mListener: OnItemClickListener
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bindData(
            image: String,
            position: Int,
            context: Context,
            isIconUpdate: Boolean,
            adapter: LogoAdapter,
            selected_position: Int
        ) {
            if (image.contains(context.getString(R.string.http)) || image.contains(
                    context.getString(
                        R.string.https
                    )
                )
            ) {
                    Glide.with(context).load(image).into(binding.imageItem)
                }
                else
                {
                    val uri: Uri = Uri.parse(image)
                    binding.imageItem.setImageURI(uri)
                }
                if (selected_position == position && isIconUpdate) {
                    binding.imageSelectedIcon.visibility = View.VISIBLE
                } else {
                    binding.imageSelectedIcon.visibility = View.INVISIBLE
                }

                binding.imageItem.setOnClickListener {
                    val previousItem: Int = selected_position

                    adapter.updateSelectedPosition(position)
                    adapter.notifyItemChanged(previousItem)
                    adapter.notifyItemChanged(position)

                    mListener.onItemClick(position - 1)
                }
            }
        }

        class AddItemViewHolder(
            private val binding: AddItemLayoutBinding,
            private val mListener: OnItemClickListener
        ) :
            RecyclerView.ViewHolder(binding.root) {

            fun bindData(position: Int) {
                binding.addCardView.setOnClickListener {
                    mListener.onAddItemClick(position)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == 0) {
                val addItemLayoutBinding =
                    AddItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

                AddItemViewHolder(addItemLayoutBinding, mListener?: throw IllegalStateException("OnItemClickListener not set"))
            } else {
                val imageItemRowBinding =
                    ImageItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)

                ItemViewHolder(imageItemRowBinding, mListener?: throw IllegalStateException("OnItemClickListener not set"))
            }
        }

        fun updateIcon(flag: Boolean) {
            isIconUpdate = flag
        }

        fun updateAdapter(position: Int) {
            selected_position += 1
            notifyItemInserted(position)
            notifyDataSetChanged()
        }

        private fun updateSelectedPosition(newPosition: Int) {
            selected_position = newPosition
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) 0 else 1
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            when (holder.itemViewType) {
                0 -> {
                    val addViewHolder = holder as AddItemViewHolder

                    addViewHolder.bindData(position)
                }
                else -> {

                    val image = logoList[position - 1]
                    val viewHolder = holder as ItemViewHolder
                    viewHolder.bindData(
                        image,
                        position,
                        context,
                        isIconUpdate,
                        this,
                        selected_position
                    )

                }
            }
        }


        override fun getItemCount(): Int = logoList.size


    }