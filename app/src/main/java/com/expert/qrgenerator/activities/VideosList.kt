package com.expert.qrgenerator.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class VideosList : AppCompatActivity() {


    // var arrayList = ArrayList<UrlData>()
    var arrayList = ArrayList<UrlData>()
    var firstUrl = ""
    var videosListAdapter: VideosListAdapter? = null
    var recyclerView: RecyclerView? = null

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos_list)
        FirebaseApp.initializeApp(this)
        val recyclerView = findViewById(R.id.recyclerView) as RecyclerView

        val rootRef = FirebaseDatabase.getInstance().reference
        val moviesRef = rootRef.child("ruvideos")
        Log.wtf("ROOT_FOUND_THE_VALUE", "VALUE$moviesRef")

        val authClass = NewFirebaseClass()
        authClass.getVidoes(recyclerView, videosListAdapter, this)


    }
}