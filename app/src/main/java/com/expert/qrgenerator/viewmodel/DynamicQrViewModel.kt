package com.expert.qrgenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DynamicQrViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    private val _dynamicQrCodeResponse = MutableLiveData<JsonObject>()
    val dynamicQrCodeResponse:LiveData<JsonObject> get() = _dynamicQrCodeResponse

    suspend fun createDynamicQrCode(body:HashMap<String,String>){
        _dynamicQrCodeResponse.postValue(apiRepository.createDynamicQrCode(body))
    }

}