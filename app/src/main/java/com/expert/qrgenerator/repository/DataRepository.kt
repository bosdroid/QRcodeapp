package com.expert.qrgenerator.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.utils.Constants
import com.google.firebase.database.*

class DataRepository {

    private lateinit var databaseReference: DatabaseReference

    init {
        databaseReference = FirebaseDatabase.getInstance().reference
    }

    companion object {
        lateinit var context: Context

        private var dataRespository: DataRepository? = null

        fun getInstance(mContext: Context): DataRepository {
            context = mContext
            if (dataRespository == null) {
                dataRespository = DataRepository()
            }
            return dataRespository!!
        }
    }


    // THIS FUNCTION WILL FETCH THE FIREBASE BACKGROUND IMAGES
    fun getBackgroundImages(): MutableLiveData<List<String>> {

        val backgroundImages = MutableLiveData<List<String>>()
        val imageList = mutableListOf<String>()

        databaseReference.child(Constants.firebaseBackgroundImages)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {

                        for (postSnapshot in dataSnapshot.children) {
                            val url = postSnapshot.getValue(String::class.java)
                            imageList.add(url!!)
                        }
                        backgroundImages.postValue(imageList)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TEST199", "loadPost:onCancelled", databaseError.toException())
                    backgroundImages.postValue(null)
                }
            })

        return backgroundImages
    }

    // THIS FUNCTION WILL FETCH THE FIREBASE LOGO IMAGES
    fun getLogoImages():MutableLiveData<List<String>>{
        val logoImages = MutableLiveData<List<String>>()
        val logoList = mutableListOf<String>()

        databaseReference.child(Constants.firebaseLogoImages)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {

                        for (postSnapshot in dataSnapshot.children) {
                            val url = postSnapshot.getValue(String::class.java)
                            logoList.add(url!!)
                        }
                        logoImages.postValue(logoList)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TEST199", "loadPost:onCancelled", databaseError.toException())
                    logoImages.postValue(null)
                }
            })

        return logoImages
    }

    // THIS FUNCTION WILL FETCH THE FIREBASE FONT LIST
    fun getFontList():MutableLiveData<List<Fonts>>{
        val fontList = MutableLiveData<List<Fonts>>()
        val list = mutableListOf<Fonts>()

        databaseReference.child(Constants.firebaseFonts)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {

                        for (postSnapshot in dataSnapshot.children) {

                            val font = Fonts(postSnapshot.child("image_url").getValue(String::class.java)!!,
                                postSnapshot.child("font_url").getValue(String::class.java)!!)
                            list.add(font)

                        }
                        fontList.postValue(list)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TEST199", "loadPost:onCancelled", databaseError.toException())
                    fontList.postValue(null)
                }
            })

        return fontList
    }


}