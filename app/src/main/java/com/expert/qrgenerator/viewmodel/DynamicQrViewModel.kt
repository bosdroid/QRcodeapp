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
class DynamicQrViewModel @Inject constructor(
    private val apiRepository: ApiRepository
) : ViewModel() {

    // LiveData to observe QR code creation responses
    private val _dynamicQrCodeResponse = MutableLiveData<JsonObject?>()
    val dynamicQrCodeResponse: LiveData<JsonObject?> get() = _dynamicQrCodeResponse

    /**
     * Function to create a dynamic QR code.
     *
     * @param body A map containing the parameters needed to create the QR code.
     * This function posts the result of the API call to LiveData.
     */
    fun createDynamicQrCode(body: HashMap<String, String>) {
        viewModelScope.launch {
            // Fetch QR code data and post to LiveData
            val response = apiRepository.createDynamicQrCode(body)
            _dynamicQrCodeResponse.postValue(response!!)
        }
    }
}
