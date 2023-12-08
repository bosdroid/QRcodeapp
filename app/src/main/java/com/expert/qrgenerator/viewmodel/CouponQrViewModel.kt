package com.expert.qrgenerator.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CouponQrViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    private var couponQrCodeResponse = MutableLiveData<JsonObject>()

    fun createCouponQrCode(body:HashMap<String,String>){
        couponQrCodeResponse = apiRepository.createCouponQrCode(body)
    }

    fun getCouponQrCode(): MutableLiveData<JsonObject> {
        return couponQrCodeResponse
    }

}