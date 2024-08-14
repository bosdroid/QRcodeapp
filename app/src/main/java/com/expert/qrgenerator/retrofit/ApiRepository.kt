package com.expert.qrgenerator.retrofit

import com.expert.qrgenerator.model.FeedbackResponse
import com.expert.qrgenerator.model.SNPayload
import com.google.gson.JsonObject

interface ApiRepository {

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING DYNAMIC QR
    suspend fun createDynamicQrCode(body: HashMap<String, String>): JsonObject?

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING COUPON QR
    suspend fun createCouponQrCode(body: HashMap<String, String>): JsonObject?

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING COUPON QR
    suspend fun createFeedbackQrCode(body: HashMap<String, String>): JsonObject?

    suspend fun signUp(body: HashMap<String, String>): JsonObject?

    suspend fun signIn(email: String): JsonObject?

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING SOCIAL NETWORK QR
    suspend fun createSnTemplate(body: SNPayload): JsonObject?

    suspend fun getAllFeedbacks(id: String): FeedbackResponse?

}