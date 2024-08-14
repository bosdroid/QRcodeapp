package com.expert.qrgenerator.retrofit

import com.expert.qrgenerator.model.FeedbackResponse
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiServices {

    // THIS IS THE POST REQUEST SERVICE FOR CREATING DYNAMIC QR CODE
    @POST("service/user/add")
    suspend fun createDynamicQrCode(@Body body: JsonObject):Response<JsonObject?>

    // THIS IS THE POST REQUEST SERVICE FOR CREATING DYNAMIC QR CODE
    @POST("service/webpage/create/template1")
    suspend fun createCouponQrCode(@Body body: JsonObject):Response<JsonObject?>

    // THIS IS THE POST REQUEST SERVICE FOR CREATING FEEDBACK QR CODE
    @POST("service/webpage/create/feedbacktemplate")
    suspend fun createFeedbackQrCode(@Body body: JsonObject):Response<JsonObject?>

    @POST("service/user/google/add")
    suspend fun signUp(@Body body: JsonObject):Response<JsonObject?>

    @GET("service/user/google/{email}")
    suspend fun signIn(@Path("email") email:String):Response<JsonObject?>

    // THIS IS THE POST REQUEST SERVICE FOR CREATING SOCIAL NETWORK QR CODE
    @POST("service/webpage/create/sntemplate")
    suspend fun createSnTemplate(@Body body: JsonObject):Response<JsonObject?>

    @GET("service/feedback/{id}")
   suspend fun getAllFeedbacks(@Path("id") id:String):Response<FeedbackResponse?>

}