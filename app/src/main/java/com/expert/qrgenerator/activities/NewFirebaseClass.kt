package com.expert.qrgenerator.activities

import android.annotation.SuppressLint
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class NewFirebaseClass {

    var rootRef = FirebaseDatabase.getInstance().reference
    var moviesRef = rootRef.child("ruvideos")

    var arrayList = ArrayList<UrlData>()


    @SuppressLint("WrongConstant")
    fun getVidoes(
        recyclerView: RecyclerView,
        videosListAdapter: VideosListAdapter?,
        videosList: VideosList
    ): ArrayList<UrlData>? {
        moviesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(UrlData::class.java)
                    arrayList.add(post!!)
                    Log.wtf("NEW_LIST_VIDEOS_DATA", "link" + arrayList.size)
                    val linearLayoutManager = LinearLayoutManager(videosList)
                    recyclerView.layoutManager = linearLayoutManager
                    val videosListAdapter = VideosListAdapter(arrayList, videosList)
                    recyclerView.adapter = videosListAdapter
                    Log.wtf("ArrayLostdata", "data" + arrayList.size);
                }




            }

            override fun onCancelled(error: DatabaseError) {}
        })










        return arrayList
    }


//
//    fun  getVidoes(recyclerView: RecyclerView, videosListAdapter: VideosListAdapter?) {
//        moviesRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (postSnapshot in dataSnapshot.children) {
//                    val post = postSnapshot.getValue(UrlData::class.java)
//                    post?.let { arrayList.add(it) }
//                    Log.wtf("NEW_LIST_VIDEOS_DATA", "link" + post!!.link)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })
//        return arrayList
//    }
}