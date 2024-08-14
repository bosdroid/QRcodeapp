package com.expert.qrgenerator.retrofit

import android.util.Log
import com.expert.qrgenerator.model.FeedbackResponse
import com.expert.qrgenerator.model.SNPayload
import com.google.gson.Gson
import com.google.gson.JsonObject
import javax.inject.Inject

class ApiRepositoryImpl @Inject constructor(private val apiServices: ApiServices) : ApiRepository {

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING DYNAMIC QR
    override suspend fun createDynamicQrCode(body: HashMap<String, String>): JsonObject? {
        val bodyJson = Gson().toJsonTree(body).asJsonObject
        Log.d("TEST199", bodyJson.toString())
        val response = apiServices.createDynamicQrCode(bodyJson)
        if (response.isSuccessful) {
            return response.body()?.asJsonObject
        }
        return null
    }

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING COUPON QR
    override suspend fun createCouponQrCode(body: HashMap<String, String>): JsonObject? {
        val bodyJson = Gson().toJsonTree(body).asJsonObject
        Log.d("TEST199", bodyJson.toString())
        val response = apiServices.createCouponQrCode(bodyJson)
        if (response.isSuccessful) {
            return response.body()?.asJsonObject
        }
        return null
    }

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING COUPON QR
    override suspend fun createFeedbackQrCode(body: HashMap<String, String>): JsonObject? {
        val bodyJson = Gson().toJsonTree(body).asJsonObject
        Log.d("TEST199", bodyJson.toString())

        val response = apiServices.createFeedbackQrCode(bodyJson)
        if (response.isSuccessful) {
            return response.body()?.asJsonObject
        }
        return null
    }

    override suspend fun signUp(body: HashMap<String, String>): JsonObject? {
        val bodyJson = Gson().toJsonTree(body).asJsonObject
        Log.d("TEST199", bodyJson.toString())
        val response = apiServices.signUp(bodyJson)
        if (response.isSuccessful) {
            return response.body()?.asJsonObject
        }
        return null
    }

    override suspend fun signIn(email: String): JsonObject? {
        val response = apiServices.signIn(email)
        if (response.isSuccessful) {
            return response.body()?.asJsonObject
        }
        return null
    }

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING SOCIAL NETWORK QR
    override suspend fun createSnTemplate(body: SNPayload): JsonObject? {
        val bodyJson = Gson().toJsonTree(body).asJsonObject
        val response = apiServices.createSnTemplate(bodyJson)
        if (response.isSuccessful) {
            return response.body()?.asJsonObject
        }
        return null
    }

    override suspend fun getAllFeedbacks(id: String): FeedbackResponse? {
        val response = apiServices.getAllFeedbacks(id)
        if (response.isSuccessful) {
            return response.body()
        }
        return null
    }

}