package com.expert.qrgenerator.viewmodel

import JSONResponse
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject

class DynamicQrViewModel : ViewModel() {

    private var dynamicQrCodeResponse = MutableLiveData<JsonObject>()

    fun createDynamicQrCode(context: Context, body:HashMap<String,String>){
        dynamicQrCodeResponse = ApiRepository.getInstance(context).createDynamicQrCode(body)
    }

    fun getDynamicQrCode():MutableLiveData<JsonObject>{
        return dynamicQrCodeResponse
    }

}