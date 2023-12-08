package com.expert.qrgenerator.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeedbackQrViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    private var feedbackQrCodeResponse = MutableLiveData<JsonObject>()

    fun createFeedbackQrCode(body:HashMap<String,String>){
        feedbackQrCodeResponse = apiRepository.createFeedbackQrCode(body)
    }

    fun getFeedbackQrCode(): MutableLiveData<JsonObject> {
        return feedbackQrCodeResponse
    }

}