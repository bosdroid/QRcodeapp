package com.expert.qrgenerator.viewmodel

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
class FeedbackQrViewModel @Inject constructor(
    private val apiRepository: ApiRepository
) : ViewModel() {

    // LiveData to observe the QR code response
    private val _feedbackQrCodeResponse = MutableLiveData<JsonObject?>()
    val feedbackQrCodeResponse: LiveData<JsonObject?> get() = _feedbackQrCodeResponse

    /**
     * Creates a feedback QR code by making a network request.
     *
     * @param body The request body containing necessary parameters.
     *
     * This function posts the response from the API to the LiveData.
     */
    fun createFeedbackQrCode(body: HashMap<String, String>) {
        viewModelScope.launch {
            try {
                // Post the response to LiveData
                _feedbackQrCodeResponse.postValue(apiRepository.createFeedbackQrCode(body))
            } catch (exception: Exception) {
                // Handle any potential exceptions
                _feedbackQrCodeResponse.postValue(null)
                // Optionally, log the exception or show an error message
            }
        }
    }
}
