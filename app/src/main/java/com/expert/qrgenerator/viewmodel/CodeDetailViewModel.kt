package com.expert.qrgenerator.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.model.FeedbackResponse
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class CodeDetailViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    var feedbackResponse = MutableLiveData<FeedbackResponse>()
    private var dynamicQrCodeResponse = MutableLiveData<JsonObject>()

    fun createDynamicQrCode(body:HashMap<String,String>){
        dynamicQrCodeResponse = apiRepository.createDynamicQrCode(body)
    }

    fun getDynamicQrCode():MutableLiveData<JsonObject>{
        return dynamicQrCodeResponse
    }

    fun callFeedbacks(id:String){
        feedbackResponse = apiRepository.getAllFeedbacks(id)
    }

    fun getAllFeedbacks():MutableLiveData<FeedbackResponse>{
        return feedbackResponse
    }

}