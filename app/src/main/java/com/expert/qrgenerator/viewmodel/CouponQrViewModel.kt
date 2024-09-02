package com.expert.qrgenerator.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponQrViewModel @Inject constructor(
    private val apiRepository: ApiRepository
) : ViewModel() {

    // LiveData to hold the coupon QR code response
    private val _couponQrCodeResponse = MutableLiveData<JsonObject?>()
    val couponQrCodeResponse: LiveData<JsonObject?> get() = _couponQrCodeResponse

    /**
     * Creates a coupon QR code by sending a request to the API.
     * Updates the LiveData with the response from the API.
     *
     * @param body A map of key-value pairs to be sent as the request body.
     */
    fun createCouponQrCode(body: HashMap<String, String>) {
        // Launch a coroutine to perform the network request
        viewModelScope.launch {
            try {
                // Perform the API call and update the LiveData with the response
                val response = apiRepository.createCouponQrCode(body)
                _couponQrCodeResponse.postValue(response!!)
            } catch (e: Exception) {
                // Handle exceptions (e.g., logging or notifying the user)
                Log.e("CouponQrViewModel", "Error creating coupon QR code", e)
            }
        }
    }
}
