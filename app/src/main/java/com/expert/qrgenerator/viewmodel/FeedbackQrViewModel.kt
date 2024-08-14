package com.expert.qrgenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeedbackQrViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    private val _feedbackQrCodeResponse = MutableLiveData<JsonObject>()
    val feedbackQrCodeResponse:LiveData<JsonObject> get() = _feedbackQrCodeResponse

    suspend fun createFeedbackQrCode(body:HashMap<String,String>){
        _feedbackQrCodeResponse.postValue(apiRepository.createFeedbackQrCode(body))
    }

}