package com.expert.qrgenerator.repository

import android.util.Log
import com.expert.qrgenerator.interfaces.BackgroundImagesCallback
import com.expert.qrgenerator.interfaces.FontsCallback
import com.expert.qrgenerator.interfaces.LogoImagesCallback
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.utils.Constants
import com.google.firebase.database.*

object DataRepository {

    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    // THIS FUNCTION WILL FETCH THE FIREBASE BACKGROUND IMAGES
    fun getBackgroundImages(callback: BackgroundImagesCallback) {
        val imageList = mutableListOf<String>()

        databaseReference.child(Constants.firebaseBackgroundImages)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (postSnapshot in dataSnapshot.children) {
                            val url = postSnapshot.getValue(String::class.java)
                            url?.let { imageList.add(it) }
                        }
                        callback.onBackgroundImagesLoaded(imageList)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TEST199", "loadPost:onCancelled", databaseError.toException())
                    callback.onBackgroundImagesError()
                }
            })
    }

    // THIS FUNCTION WILL FETCH THE FIREBASE LOGO IMAGES
    fun getLogoImages(callback: LogoImagesCallback) {
        databaseReference.child(Constants.firebaseLogoImages)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val logoList = mutableListOf<String>()
                        for (postSnapshot in dataSnapshot.children) {
                            val url = postSnapshot.getValue(String::class.java)
                            url?.let { logoList.add(it) }
                        }
                        callback.onLogoImagesLoaded(logoList)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TEST199", "loadPost:onCancelled", databaseError.toException())
                    callback.onLogoImagesError()
                }
            })
    }

    // THIS FUNCTION WILL FETCH THE FIREBASE FONT LIST
    fun getFontList(callback: FontsCallback) {
        val list = mutableListOf<Fonts>()

        databaseReference.child(Constants.firebaseFonts)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (postSnapshot in dataSnapshot.children) {
                            val font = Fonts(
                                postSnapshot.child("image_url").getValue(String::class.java)!!,
                                postSnapshot.child("font_url").getValue(String::class.java)!!
                            )
                            list.add(font)
                        }
                        callback.onFontsLoaded(list)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("TEST199", "loadPost:onCancelled", databaseError.toException())
                    callback.onFontsError()
                }
            })
    }

}