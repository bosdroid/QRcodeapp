package com.expert.qrgenerator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.expert.qrgenerator.R
import com.expert.qrgenerator.databinding.FontFamilyItemRowBinding
import com.expert.qrgenerator.model.Fonts

class FontAdapter(private val context: Context, private val fontList:List<Fonts>):RecyclerView.Adapter<FontAdapter.ItemViewHolder>() {


    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
    private var mListener: OnItemClickListener?=null
    private var isIconUpdate:Boolean = false
    private var selected_position = -1

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.mListener = listener
    }

    fun updateIcon(flag:Boolean)
    {
        isIconUpdate = flag
    }

    class ItemViewHolder(private val binding: FontFamilyItemRowBinding,private val mListener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root)
    {

          fun bindData(position: Int,font: Fonts,context: Context,isIconUpdate:Boolean,adapter: FontAdapter,selected_position:Int){
                Glide.with(context).load(font.fontImage).into(binding.fontItem)
                if (selected_position == position && isIconUpdate)
                {
                    binding.selectedIcon.visibility = View.VISIBLE
                }
                else
                {
                    binding.selectedIcon.visibility = View.INVISIBLE
                }

                binding.fontItem.setOnClickListener {
                    val previousItem: Int = selected_position

                     adapter.updateSelectedPosition(position)
                    adapter.notifyItemChanged(previousItem)
                    adapter.notifyItemChanged(position)

                    mListener.onItemClick(position)
                }
          }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val fontFamilyItemRowBinding = FontFamilyItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(fontFamilyItemRowBinding, mListener?: throw IllegalStateException("OnItemClickListener not set"))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val font = fontList[position]
        holder.bindData(position,font,context,isIconUpdate,this,selected_position)

    }

    private fun updateSelectedPosition(newPosition: Int){
        selected_position = newPosition
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = fontList.size

}