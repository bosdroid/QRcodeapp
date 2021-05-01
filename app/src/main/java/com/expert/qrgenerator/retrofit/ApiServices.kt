package com.expert.qrgenerator.retrofit

import JSONResponse
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiServices {

    // THIS IS THE POST REQUEST SERVICE FOR CREATING DYNAMIC QR CODE
    @POST("service/user/add")
    fun createDynamicQrCode(@Body body: JsonObject):Call<JsonObject>

    // THIS IS THE POST REQUEST SERVICE FOR CREATING DYNAMIC QR CODE
    @POST("service/webpage/create/template1")
    fun createCouponQrCode(@Body body: JsonObject):Call<JsonObject>

    // THIS IS THE POST REQUEST SERVICE FOR CREATING FEEDBACK QR CODE
    @POST("service/webpage/create/feedbacktemplate")
    fun createFeedbackQrCode(@Body body: JsonObject):Call<JsonObject>


}