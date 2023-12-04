package com.expert.qrgenerator.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.repository.DataRepository
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DesignActivityViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    private var colorList = MutableLiveData<List<String>>()
    private var backgroundImageList = MutableLiveData<List<String>>()
    private var logoImageList = MutableLiveData<List<String>>()
    private var fontList = MutableLiveData<List<Fonts>>()
//    private var dynamicQrCodeResponse = MutableLiveData<JsonObject>()

    // THIS FUNCTION WILL CREATE AND SAVE THE COLOR LIST
    fun callColorList(context: Context) {
        val colorArray = context.resources.getStringArray(R.array.color_values)
        val tempList = mutableListOf<String>()
        for (value in colorArray) {
            tempList.add(value)
        }
        colorList.postValue(tempList)
    }

    // THIS FUNCTION WILL RETURN THE COLOR LIST
    fun getColorList(): LiveData<List<String>> {
        return colorList
    }

    // THIS FUNCTION WILL CALL THE BACKGROUND IMAGE LIST FROM DATA REPOSITORY
    fun callBackgroundImages(){
        backgroundImageList = DataRepository.getBackgroundImages()
    }

    // THIS FUNCTION WILL RETURN THE IMAGE LIST
    fun getBackgroundImages(): LiveData<List<String>> {
        return backgroundImageList
    }

    // THIS FUNCTION WILL CALL THE LOGO IMAGE LIST FROM DATA REPOSITORY
    fun callLogoImages(){
        logoImageList = DataRepository.getLogoImages()
    }

    // THIS FUNCTION WILL RETURN THE LOGO LIST
    fun getLogoImages(): LiveData<List<String>> {
        return logoImageList
    }

    // THIS FUNCTION WILL CALL THE FONT LIST FROM DATA REPOSITORY
    fun callFontList(){
        fontList = DataRepository.getFontList()
    }

    // THIS FUNCTION WILL RETURN THE FONT LIST
    fun getFontList(): LiveData<List<Fonts>> {
        return fontList
    }
}