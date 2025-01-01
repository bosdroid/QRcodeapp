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
class MainActivityViewModel @Inject constructor(
    private val apiRepository: ApiRepository
) : ViewModel() {

    // LiveData objects to observe API responses
    private val _dynamicQrCodeResponse = MutableLiveData<JsonObject?>()
    private val _signUpResponse = MutableLiveData<JsonObject?>()
    private val _signInResponse = MutableLiveData<JsonObject?>()

    // Publicly exposed LiveData for observing API responses
    val dynamicQrCodeResponse: LiveData<JsonObject?> get() = _dynamicQrCodeResponse
    val signUpResponse: LiveData<JsonObject?> get() = _signUpResponse
    val signInResponse: LiveData<JsonObject?> get() = _signInResponse

    /**
     * Creates a dynamic QR code by calling the repository's API method.
     *
     * @param body A map containing the parameters required for creating a dynamic QR code.
     */
    fun createDynamicQrCode(body: HashMap<String, String>) {
        viewModelScope.launch {
            try {
                val response = apiRepository.createDynamicQrCode(body)
                _dynamicQrCodeResponse.postValue(response!!)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    /**
     * Signs up a user by calling the repository's API method.
     *
     * @param body A map containing the parameters required for signing up.
     */
    fun signUp(body: HashMap<String, String>) {
        viewModelScope.launch {
            try {
                val response = apiRepository.signUp(body)
                _signUpResponse.postValue(response!!)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    /**
     * Signs in a user by calling the repository's API method.
     *
     * @param email The email of the user signing in.
     */
    fun signIn(email: String) {
        viewModelScope.launch {
            try {
                val response = apiRepository.signIn(email)
                _signInResponse.postValue(response!!)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun saveFcmToken(body: HashMap<String, String>){
        viewModelScope.launch {
            apiRepository.saveFcmToken(body)
        }
    }
}
