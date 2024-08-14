package com.expert.qrgenerator.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.databinding.SnIconItemRowDesignBinding

class SNIconsAdapter(private val iconsList:ArrayList<Pair<String,Int>>):RecyclerView.Adapter<SNIconsAdapter.ItemViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(pos: Int)
    }

    private var mListener: OnItemClickListener?=null

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.mListener = listener
    }

    inner class ItemViewHolder(private val binding: SnIconItemRowDesignBinding,private val mListener: OnItemClickListener):RecyclerView.ViewHolder(binding.root){
                fun bindData(pair : Pair<String, Int>){
                    binding.snIconItemView.setImageResource(pair.second)
                    binding.snIconItemView.setOnClickListener {
                        mListener.onItemClick(layoutPosition)
                    }
                }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val snIconItemRowDesignBinding = SnIconItemRowDesignBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ItemViewHolder(snIconItemRowDesignBinding,mListener?: throw IllegalStateException("OnItemClickListener not set"))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val pair = iconsList[position]
        holder.bindData(pair)
    }

    override fun getItemCount(): Int = iconsList.size

}