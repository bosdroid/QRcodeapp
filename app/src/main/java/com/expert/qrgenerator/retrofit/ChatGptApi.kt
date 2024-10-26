package com.expert.qrgenerator.retrofit

import com.expert.qrgenerator.model.ChatGptRequest
import com.expert.qrgenerator.model.ChatGptResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ChatGptApi {
    @POST("v1/chat/completions")
    suspend fun getRecommendations(
        @Header("Authorization") auth: String,
        @Body request: ChatGptRequest
    ): Response<ChatGptResponse>
}