package com.expert.qrgenerator.retrofit

import JSONResponse
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.expert.qrgenerator.model.FeedbackResponse
import com.expert.qrgenerator.model.SNPayload
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface ApiRepository {


    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING DYNAMIC QR
    fun createDynamicQrCode(body: HashMap<String, String>): MutableLiveData<JsonObject>

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING COUPON QR
    fun createCouponQrCode(body: HashMap<String, String>): MutableLiveData<JsonObject>

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING COUPON QR
    fun createFeedbackQrCode(body: HashMap<String, String>): MutableLiveData<JsonObject>

    fun signUp(body: HashMap<String, String>): MutableLiveData<JsonObject>

    fun signIn(email: String): MutableLiveData<JsonObject>

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING SOCIAL NETWORK QR
    fun createSnTemplate(body: SNPayload): MutableLiveData<JsonObject>

    fun getAllFeedbacks(id: String): MutableLiveData<FeedbackResponse>

}