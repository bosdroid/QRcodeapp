package com.expert.qrgenerator.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expert.qrgenerator.retrofit.ApiRepository
import com.expert.qrgenerator.utils.SingleEvent
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VCardViewModel @Inject constructor(
    private val apiRepository: ApiRepository
) : ViewModel() {

    // LiveData to observe QR code creation responses
    private val _vCardResponse = MutableLiveData<SingleEvent<JsonObject?>>()
    val vCardResponse: LiveData<SingleEvent<JsonObject?>> get() = _vCardResponse

    // LiveData to observe QR code creation responses
    private val _existVCardResponse = MutableLiveData<SingleEvent<JsonObject?>>()
    val existVCardResponse: LiveData<SingleEvent<JsonObject?>> get() = _existVCardResponse

    /**
     * Function to create a vCard webpage.
     *
     * @param body A map containing the parameters needed to create the vCard webpage.
     * This function posts the result of the API call to LiveData.
     */
    fun createVCard(body: HashMap<String, String>) {
        Log.d("TEST1999","hit create vcard")
        viewModelScope.launch {
            // Fetch QR code data and post to LiveData
            val response = apiRepository.createVCard(body)
            _vCardResponse.postValue(SingleEvent(response))
        }
    }

    fun existVCard(body: HashMap<String, String>) {
        Log.d("TEST1999","hit create vcard")
        viewModelScope.launch {
            // Fetch QR code data and post to LiveData
            val response = apiRepository.createVCard(body)
            _existVCardResponse.postValue(SingleEvent(response))
        }
    }
}
