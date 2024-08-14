package com.expert.qrgenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    private var _dynamicQrCodeResponse = MutableLiveData<JsonObject>()
    private var _signUpResponse = MutableLiveData<JsonObject>()
    private var _signInResponse = MutableLiveData<JsonObject>()

    val dynamicQrCodeResponse:LiveData<JsonObject> get() = _dynamicQrCodeResponse
    val signUpResponse :LiveData<JsonObject> get() = _signUpResponse
    val signInResponse : LiveData<JsonObject> get() = _signInResponse


    suspend fun createDynamicQrCode(body:HashMap<String,String>){
        _dynamicQrCodeResponse.postValue(apiRepository.createDynamicQrCode(body))
    }

    suspend fun signUp(body:HashMap<String,String>){
        _signUpResponse.postValue(apiRepository.signUp(body))
    }


    suspend fun signIn(email:String){
        _signInResponse.postValue(apiRepository.signIn(email))
    }


}