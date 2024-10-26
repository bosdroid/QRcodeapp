package com.expert.qrgenerator.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expert.qrgenerator.interfaces.TrackableScansCallback
import com.expert.qrgenerator.model.ChatGptRequest
import com.expert.qrgenerator.model.FeedbackResponse
import com.expert.qrgenerator.model.Message
import com.expert.qrgenerator.model.TrackableScan
import com.expert.qrgenerator.repository.DataRepository
import com.expert.qrgenerator.retrofit.ApiRepository
import com.expert.qrgenerator.retrofit.ChatGptApi
import com.expert.qrgenerator.utils.Constants
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

@HiltViewModel
class CodeDetailViewModel @Inject constructor(
    private val apiRepository: ApiRepository
) : ViewModel() {

    // LiveData to observe feedback responses
    private val _aiRecommendationResponse = MutableLiveData<String?>()
    val aiRecommendationResponse: LiveData<String?> get() = _aiRecommendationResponse


    // LiveData to observe feedback responses
    private val _feedbackResponse = MutableLiveData<FeedbackResponse?>()
    val feedbackResponse: LiveData<FeedbackResponse?> get() = _feedbackResponse

    // LiveData to observe dynamic QR code responses
    private val _dynamicQrCodeResponse = MutableLiveData<JsonObject?>()
    val dynamicQrCodeResponse: LiveData<JsonObject?> get() = _dynamicQrCodeResponse

    // LiveData to hold the list of Trackable Scans
    private val _trackableScanList = MutableLiveData<List<TrackableScan>>()
    val trackableScanList: LiveData<List<TrackableScan>> get() = _trackableScanList

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

    /**
     * Fetches the background images from the data repository.
     */
    fun callTrackableScans(qrId:String) {
        DataRepository.getAllScanHistory(qrId, object : TrackableScansCallback {
            override fun onTrackableScansLoaded(trackableScanList: List<TrackableScan>) {
                _trackableScanList.postValue(trackableScanList)
            }

            override fun onTrackableScansError() {
                _trackableScanList.postValue(emptyList())
            }
        })
    }

    suspend fun callAiRecommendationRequest(prompt: String,callback:(String)->Unit){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val chatGptApi = retrofit.create(ChatGptApi::class.java)

        viewModelScope.launch {
            val apiKey = Constants.chatGptApiKey

            val recommendations = getChatGptRecommendations(apiKey, prompt,chatGptApi)

            recommendations?.let {
                println(it)
                callback(it)
            } ?: run {
                callback("Failed to get recommendations")
            }

        }
    }

    private suspend fun getChatGptRecommendations(apiKey: String, userInput: String, chatGptApi:ChatGptApi): String? {
        val request = ChatGptRequest(
            model = "gpt-3.5-turbo", // Use the appropriate model name
            messages = listOf(
                Message(role = "user", content = userInput)
            )
        )

        val response = chatGptApi.getRecommendations("Bearer $apiKey", request)

        return if (response.isSuccessful) {
            response.body()?.choices?.firstOrNull()?.message?.content
        } else {
            // Handle the error
            null
        }
    }

}
