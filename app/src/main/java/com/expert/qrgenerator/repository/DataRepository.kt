package com.expert.qrgenerator.repository

import android.util.Log
import com.expert.qrgenerator.interfaces.BackgroundImagesCallback
import com.expert.qrgenerator.interfaces.FontsCallback
import com.expert.qrgenerator.interfaces.LogoImagesCallback
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.utils.Constants
import com.google.firebase.database.*

object DataRepository {

    // Firebase Database reference
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    /**
     * Fetches the list of background images from Firebase.
     *
     * @param callback Callback to handle the result or error.
     */
    fun getBackgroundImages(callback: BackgroundImagesCallback) {
        val imageList = mutableListOf<String>()

        databaseReference.child(Constants.FIREBASE_BACKGROUND_IMAGES)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (postSnapshot in dataSnapshot.children) {
                            postSnapshot.getValue(String::class.java)?.let { imageList.add(it) }
                        }
                        callback.onBackgroundImagesLoaded(imageList)
                    } else {
                        callback.onBackgroundImagesError() // Handle case when no data exists
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("DataRepository", "Error fetching background images", databaseError.toException())
                    callback.onBackgroundImagesError()
                }
            })
    }

    /**
     * Fetches the list of logo images from Firebase.
     *
     * @param callback Callback to handle the result or error.
     */
    fun getLogoImages(callback: LogoImagesCallback) {
        val logoList = mutableListOf<String>()

        databaseReference.child(Constants.FIREBASE_LOGO_IMAGES)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (postSnapshot in dataSnapshot.children) {
                            postSnapshot.getValue(String::class.java)?.let { logoList.add(it) }
                        }
                        callback.onLogoImagesLoaded(logoList)
                    } else {
                        callback.onLogoImagesError() // Handle case when no data exists
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("DataRepository", "Error fetching logo images", databaseError.toException())
                    callback.onLogoImagesError()
                }
            })
    }

    /**
     * Fetches the list of fonts from Firebase.
     *
     * @param callback Callback to handle the result or error.
     */
    fun getFontList(callback: FontsCallback) {
        val fontList = mutableListOf<Fonts>()

        databaseReference.child(Constants.FIREBASE_FONTS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (postSnapshot in dataSnapshot.children) {
                            val imageUrl = postSnapshot.child("image_url").getValue(String::class.java)
                            val fontUrl = postSnapshot.child("font_url").getValue(String::class.java)
                            if (imageUrl != null && fontUrl != null) {
                                fontList.add(Fonts(imageUrl, fontUrl))
                            }
                        }
                        callback.onFontsLoaded(fontList)
                    } else {
                        callback.onFontsError() // Handle case when no data exists
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("DataRepository", "Error fetching fonts", databaseError.toException())
                    callback.onFontsError()
                }
            })
    }
}
