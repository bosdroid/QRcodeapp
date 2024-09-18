package com.expert.qrgenerator.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expert.qrgenerator.R
import com.expert.qrgenerator.interfaces.BackgroundImagesCallback
import com.expert.qrgenerator.interfaces.FontsCallback
import com.expert.qrgenerator.interfaces.LogoImagesCallback
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.repository.DataRepository
import com.expert.qrgenerator.retrofit.ApiRepository
import com.expert.qrgenerator.utils.ImageManager
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DesignActivityViewModel @Inject constructor(
    private val apiRepository: ApiRepository // Injected API repository
) : ViewModel() {

    // LiveData to hold the list of colors
    private val _colorList = MutableLiveData<List<String>>()
    val colorList: LiveData<List<String>> get() = _colorList

    // LiveData to hold the list of background images
    private val _backgroundImageList = MutableLiveData<List<String>>()
    val backgroundImageList: LiveData<List<String>> get() = _backgroundImageList

    // LiveData to hold the list of logo images
    private val _logoImageList = MutableLiveData<List<String>>()
    val logoImageList: LiveData<List<String>> get() = _logoImageList

    // LiveData to hold the list of fonts
    private val _fontList = MutableLiveData<List<Fonts>>()
    val fontList: LiveData<List<Fonts>> get() = _fontList

    // LiveData to observe the upload result
    private val _uploadImageResponse = MutableLiveData<JsonObject?>()
    val uploadImageResponse: LiveData<JsonObject?> get() = _uploadImageResponse

    /**
     * Creates and saves the color list from string array resources.
     *
     * @param context Context to access resources.
     */
    fun callColorList(context: Context) {
        val colorArray = context.resources.getStringArray(R.array.color_values)
        val tempList = colorArray.toList() // Convert array to list
        _colorList.postValue(tempList)
    }

    /**
     * Fetches the background images from the data repository.
     */
    fun callBackgroundImages() {
        DataRepository.getBackgroundImages(object : BackgroundImagesCallback {
            override fun onBackgroundImagesLoaded(images: List<String>) {
                _backgroundImageList.postValue(images)
            }

            override fun onBackgroundImagesError() {
                _backgroundImageList.postValue(emptyList()) // Use empty list instead of null
            }
        })
    }

    /**
     * Fetches the logo images from the data repository.
     */
    fun callLogoImages() {
        DataRepository.getLogoImages(object : LogoImagesCallback {
            override fun onLogoImagesLoaded(images: List<String>) {
                _logoImageList.postValue(images)
            }

            override fun onLogoImagesError() {
                _logoImageList.postValue(emptyList()) // Use empty list instead of null
            }
        })
    }

    /**
     * Fetches the font list from the data repository.
     */
    fun callFontList() {
        DataRepository.getFontList(object : FontsCallback {
            override fun onFontsLoaded(fonts: List<Fonts>) {
                _fontList.postValue(fonts)
            }

            override fun onFontsError() {
                _fontList.postValue(emptyList()) // Use empty list instead of null
            }
        })
    }

    fun uploadQrImage(context: Context, image:Bitmap,name:String) {
        viewModelScope.launch {

            // Get file from URI
            val file = ImageManager.readWriteImage(context,image)

            // Create a custom file name with .png extension
            val customFileName = "$name.jpg"

            // Create RequestBody instance for the image file
            val requestFile = RequestBody.create(MediaType.get("image/jpg"), file)

            // MultipartBody.Part is used to send the file with a custom name
            val body = MultipartBody.Part.createFormData("image", customFileName, requestFile)

            // Optional: Add name as RequestBody
            val rName = RequestBody.create(MediaType.get("text/plain"), "image_name")


            // Fetch QR code data and post to LiveData
            val response = apiRepository.uploadQrCodeImage(body,rName)
            _uploadImageResponse.postValue(response!!)
        }
    }
}
