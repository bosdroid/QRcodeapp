package com.expert.qrgenerator.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject

class CouponQrViewModel : ViewModel() {

    private var couponQrCodeResponse = MutableLiveData<JsonObject>()

    fun createCouponQrCode(context: Context, body:HashMap<String,String>){
        couponQrCodeResponse = ApiRepository.getInstance(context).createCouponQrCode(body)
    }

    fun getCouponQrCode(): MutableLiveData<JsonObject> {
        return couponQrCodeResponse
    }

}