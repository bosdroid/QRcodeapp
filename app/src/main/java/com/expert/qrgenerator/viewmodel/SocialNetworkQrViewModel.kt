package com.expert.qrgenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.model.SNPayload
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SocialNetworkQrViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    private val _snQrCodeResponse = MutableLiveData<JsonObject>()
    val snQrCodeResponse : LiveData<JsonObject> get() = _snQrCodeResponse

    suspend fun createSnQrCode(body: SNPayload){
        _snQrCodeResponse.postValue(apiRepository.createSnTemplate(body))
    }

}