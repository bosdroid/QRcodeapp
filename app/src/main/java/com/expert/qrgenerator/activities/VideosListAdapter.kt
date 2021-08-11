package com.expert.qrgenerator.activities

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R

class VideosListAdapter(val userList: ArrayList<UrlData>, var mContext: Context) :
    RecyclerView.Adapter<VideosListAdapter.ViewHolder>() {
    var onItemClick: ((UrlData) -> Unit)? = null

    //    private val mContext: Context? = null
    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(

        parent: ViewGroup,
        viewType: Int
    ):
            VideosListAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.videoslist, parent, false)
        mContext = parent.getContext();
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: VideosListAdapter.ViewHolder, position: Int) {
        holder.bindItems(userList[position])


//        holder.itemView.setOnClickListener {
//
//            val intent = Intent(mContext, NewExoplayerscreen::class.java)
//            intent.putExtra("key", userList.get(position).link)
//            mContext.startActivity(intent)
//        }

        holder.itemView.setOnClickListener {
            //  Toast.makeText(mContext,"clicked",Toast.LENGTH_SHORT).show()
            val intent = Intent(mContext, NewExoplayerscreen::class.java)
            intent.putExtra("key", userList.get(position).link)
            mContext.startActivity(intent)

//            val intent = Intent(mContext, VideoPlayerScreen::class.java)
//            intent.putExtra("key", userList.get(position).link)
//            mContext.startActivity(intent)


        }


    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return userList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(user: UrlData) {

            val tvVideourl = itemView.findViewById(R.id.tvvideosurl) as TextView
            val tvvideoType = itemView.findViewById(R.id.tvvideotype) as TextView
            tvVideourl.text = user.link
            tvvideoType.text = "Type :" + user.type


        }
    }
}
