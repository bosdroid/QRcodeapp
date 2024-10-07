package com.expert.qrgenerator.retrofit

import com.expert.qrgenerator.utils.Constants
import com.expert.qrgenerator.utils.getUnsafeOkHttpClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientApi {

    // Single instance of OkHttpClient to be used across multiple Retrofit instances
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // You can add additional configurations here if needed
            .build()
    }

    /**
     * Provides a singleton instance of Retrofit.
     *
     * @return A Retrofit instance configured with the base URL and Gson converter.
     */
    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL) // Set the base URL for API requests
            .addConverterFactory(GsonConverterFactory.create()) // Add Gson converter for JSON serialization/deserialization
            .client(getUnsafeOkHttpClient()) // Set the OkHttpClient instance
            .build()
    }
}
