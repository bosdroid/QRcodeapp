package com.expert.qrgenerator.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.model.SNPayload
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import org.json.JSONObject

class SocialNetworkQrViewModel : ViewModel() {

    private var snQrCodeResponse = MutableLiveData<JsonObject>()

    fun createSnQrCode(context: Context, body: SNPayload){
        snQrCodeResponse = ApiRepository.getInstance(context).createSnTemplate(body)
    }

    fun getSnQrCode(): MutableLiveData<JsonObject> {
        return snQrCodeResponse
    }

}