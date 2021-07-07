package com.expert.qrgenerator.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.model.FeedbackResponse
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject

class CodeDetailViewModel : ViewModel() {

    var feedbackResponse = MutableLiveData<FeedbackResponse>()
    private var dynamicQrCodeResponse = MutableLiveData<JsonObject>()

    fun createDynamicQrCode(context: Context, body:HashMap<String,String>){
        dynamicQrCodeResponse = ApiRepository.getInstance(context).createDynamicQrCode(body)
    }

    fun getDynamicQrCode():MutableLiveData<JsonObject>{
        return dynamicQrCodeResponse
    }

    fun callFeedbacks(context: Context,id:String){
        feedbackResponse = ApiRepository.getInstance(context).getAllFeedbacks(id)
    }

    fun getAllFeedbacks():MutableLiveData<FeedbackResponse>{
        return feedbackResponse
    }

}