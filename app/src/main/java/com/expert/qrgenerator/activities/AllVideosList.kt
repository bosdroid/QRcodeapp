package com.expert.qrgenerator.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expert.qrgenerator.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class AllVideosList : AppCompatActivity() {

    // var arrayList = ArrayList<UrlData>()
    val arrayList = ArrayList<UrlData>()
    var firstUrl = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_videos_list)
        val recyclerView = findViewById(R.id.recyclerView) as RecyclerView

        val rootRef = FirebaseDatabase.getInstance().reference
        val moviesRef = rootRef.child("ruvideos")
        Log.wtf("ROOT_FOUND_THE_VALUE", "VALUE$moviesRef")

        moviesRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("WrongConstant")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(UrlData::class.java)
                    arrayList.add(post!!)
                    Log.wtf("GetData_VALues_LIST_VALUES", "link" + arrayList.size)
                    Log.wtf("GetData_VALues", "link" + post.link)
                    recyclerView.layoutManager =
                        LinearLayoutManager(VideosList(), LinearLayout.VERTICAL, false)
                    Log.wtf("VALUES_INLIST", "list" + arrayList.size);
                    //creating our adapter
                    val adapter = VideosListAdapter(arrayList, VideosList());
                    //now adding the adapter to recyclerview
                    recyclerView.adapter = adapter
                }
                firstUrl = arrayList[0].toString()
                Log.wtf("FirstUrl_found", "foundd$firstUrl")
            }


            override fun onCancelled(error: DatabaseError) {}
        })

    }
}