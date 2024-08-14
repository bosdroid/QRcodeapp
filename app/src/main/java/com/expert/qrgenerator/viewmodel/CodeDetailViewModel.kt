package com.expert.qrgenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.model.FeedbackResponse
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CodeDetailViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    private val _feedbackResponse = MutableLiveData<FeedbackResponse>()
    private val _dynamicQrCodeResponse = MutableLiveData<JsonObject>()
    val feedbackResponse:LiveData<FeedbackResponse> get() = _feedbackResponse
    val dynamicQrCodeResponse:LiveData<JsonObject> get() = _dynamicQrCodeResponse
    suspend fun createDynamicQrCode(body:HashMap<String,String>){
        _dynamicQrCodeResponse.postValue(apiRepository.createDynamicQrCode(body))
    }


    suspend fun callFeedbacks(id:String){
        _feedbackResponse.postValue(apiRepository.getAllFeedbacks(id))
    }

}