package com.expert.qrgenerator.retrofit

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.expert.qrgenerator.model.FeedbackResponse
import com.expert.qrgenerator.model.SNPayload
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class ApiRepositoryImpl @Inject constructor(private val apiServices: ApiServices) : ApiRepository {

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING DYNAMIC QR
    override fun createDynamicQrCode(body: HashMap<String, String>): MutableLiveData<JsonObject> {
        val res = MutableLiveData<JsonObject>()
        val bodyJson = Gson().toJsonTree(body).asJsonObject
        Log.d("TEST199", bodyJson.toString())
        apiServices.createDynamicQrCode(bodyJson).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING COUPON QR
    override fun createCouponQrCode(body: HashMap<String, String>): MutableLiveData<JsonObject> {
        val res = MutableLiveData<JsonObject>()
        val bodyJson = Gson().toJsonTree(body).asJsonObject
        Log.d("TEST199", bodyJson.toString())
        apiServices.createCouponQrCode(bodyJson).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING COUPON QR
    override fun createFeedbackQrCode(body: HashMap<String, String>): MutableLiveData<JsonObject> {
        val res = MutableLiveData<JsonObject>()
        val bodyJson = Gson().toJsonTree(body).asJsonObject
        Log.d("TEST199", bodyJson.toString())
        apiServices.createFeedbackQrCode(bodyJson).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }


    override fun signUp(body: HashMap<String, String>): MutableLiveData<JsonObject> {
        val res = MutableLiveData<JsonObject>()
        val bodyJson = Gson().toJsonTree(body).asJsonObject
        Log.d("TEST199", bodyJson.toString())
        apiServices.signUp(bodyJson).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    override fun signIn(email: String): MutableLiveData<JsonObject> {
        val res = MutableLiveData<JsonObject>()
        apiServices.signIn(email).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    // THIS FUNCTION WILL SEND THE POST REQUEST TO SERVER FOR CREATING SOCIAL NETWORK QR
    override fun createSnTemplate(body: SNPayload): MutableLiveData<JsonObject> {
        val res = MutableLiveData<JsonObject>()
        val bodyJson = Gson().toJsonTree(body).asJsonObject
        apiServices.createSnTemplate(bodyJson).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

    override fun getAllFeedbacks(id: String): MutableLiveData<FeedbackResponse> {
        val res = MutableLiveData<FeedbackResponse>()
        apiServices.getAllFeedbacks(id).enqueue(object : Callback<FeedbackResponse> {
            override fun onResponse(
                call: Call<FeedbackResponse>,
                response: Response<FeedbackResponse>
            ) {
                res.postValue(response.body())
            }

            override fun onFailure(call: Call<FeedbackResponse>, t: Throwable) {
                res.postValue(null)
            }
        })

        return res
    }

}