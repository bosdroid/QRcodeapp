package com.expert.qrgenerator.retrofit

import com.expert.qrgenerator.utils.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientApi {

    private val client = OkHttpClient.Builder().build()


    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }


}