package com.expert.qrgenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CouponQrViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    private val _couponQrCodeResponse = MutableLiveData<JsonObject>()
    val couponQrCodeResponse:LiveData<JsonObject> get() = _couponQrCodeResponse

    suspend fun createCouponQrCode(body:HashMap<String,String>){
        _couponQrCodeResponse.postValue(apiRepository.createCouponQrCode(body))
    }

}