package com.expert.qrgenerator.retrofit

import JSONResponse
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiServices {

    @POST("service/user/add")
    fun createDynamicQrCode(@Body body: JsonObject):Call<JsonObject>

}