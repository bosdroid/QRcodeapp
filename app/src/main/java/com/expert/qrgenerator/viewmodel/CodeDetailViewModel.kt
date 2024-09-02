package com.expert.qrgenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.model.FeedbackResponse
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CodeDetailViewModel @Inject constructor(
    private val apiRepository: ApiRepository
) : ViewModel() {

    // LiveData to observe feedback responses
    private val _feedbackResponse = MutableLiveData<FeedbackResponse?>()
    val feedbackResponse: LiveData<FeedbackResponse?> get() = _feedbackResponse

    // LiveData to observe dynamic QR code responses
    private val _dynamicQrCodeResponse = MutableLiveData<JsonObject?>()
    val dynamicQrCodeResponse: LiveData<JsonObject?> get() = _dynamicQrCodeResponse

    /**
     * Creates a dynamic QR code using the provided parameters.
     * Updates the LiveData `_dynamicQrCodeResponse` with the result.
     *
     * @param body Parameters for creating the QR code.
     */
    suspend fun createDynamicQrCode(body: HashMap<String, String>) {
        // Post the result to LiveData
        _dynamicQrCodeResponse.postValue(apiRepository.createDynamicQrCode(body))
    }

    /**
     * Fetches all feedbacks for a given ID.
     * Updates the LiveData `_feedbackResponse` with the result.
     *
     * @param id The ID for which feedbacks are to be fetched.
     */
    suspend fun callFeedbacks(id: String) {
        // Post the result to LiveData
        _feedbackResponse.postValue(apiRepository.getAllFeedbacks(id))
    }
}
