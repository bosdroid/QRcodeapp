package com.expert.qrgenerator.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expert.qrgenerator.model.SNPayload
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialNetworkQrViewModel @Inject constructor(
    private val apiRepository: ApiRepository
) : ViewModel() {

    // LiveData for observing QR code response
    private val _snQrCodeResponse = MutableLiveData<JsonObject?>()
    val snQrCodeResponse: LiveData<JsonObject?> get() = _snQrCodeResponse

    /**
     * Creates a social network QR code by making a network request.
     *
     * @param body Payload containing the necessary data for QR code creation.
     */
    fun createSnQrCode(body: SNPayload) {
        viewModelScope.launch {
            try {
                // Make the network request and update LiveData with the response
                val response = apiRepository.createSnTemplate(body)
                _snQrCodeResponse.postValue(response)
            } catch (e: Exception) {
                // Handle exception (e.g., logging or showing an error message)
                Log.e("SocialNetworkQr", "Error creating QR code", e)
                // You might want to update LiveData to reflect the error state
                _snQrCodeResponse.postValue(null) // or some error state
            }
        }
    }
}
