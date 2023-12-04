package com.expert.qrgenerator.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.model.SNPayload
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class SocialNetworkQrViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    private var snQrCodeResponse = MutableLiveData<JsonObject>()

    fun createSnQrCode(body: SNPayload){
        snQrCodeResponse =apiRepository.createSnTemplate(body)
    }

    fun getSnQrCode(): MutableLiveData<JsonObject> {
        return snQrCodeResponse
    }

}