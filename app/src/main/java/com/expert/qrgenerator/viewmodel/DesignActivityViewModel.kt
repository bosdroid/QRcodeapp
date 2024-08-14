package com.expert.qrgenerator.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.R
import com.expert.qrgenerator.interfaces.BackgroundImagesCallback
import com.expert.qrgenerator.interfaces.FontsCallback
import com.expert.qrgenerator.interfaces.LogoImagesCallback
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.repository.DataRepository
import com.expert.qrgenerator.retrofit.ApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DesignActivityViewModel @Inject constructor(private val apiRepository: ApiRepository) : ViewModel() {

    private var _colorList = MutableLiveData<List<String>>()
    val colorList: MutableLiveData<List<String>>
        get() = _colorList
    private var _backgroundImageList = MutableLiveData<List<String>>()
    val backgroundImageList: MutableLiveData<List<String>>
        get() = _backgroundImageList
    private var _logoImageList = MutableLiveData<List<String>>()
    val logoImageList: MutableLiveData<List<String>>
        get() = _logoImageList
    private var _fontList = MutableLiveData<List<Fonts>>()
    val fontList: MutableLiveData<List<Fonts>>
        get() = _fontList

    // THIS FUNCTION WILL CREATE AND SAVE THE COLOR LIST
    fun callColorList(context: Context) {
        val colorArray = context.resources.getStringArray(R.array.color_values)
        val tempList = mutableListOf<String>()
        for (value in colorArray) {
            tempList.add(value)
        }
        _colorList.postValue(tempList)
    }

    // THIS FUNCTION WILL CALL THE BACKGROUND IMAGE LIST FROM DATA REPOSITORY
    fun callBackgroundImages(){
        DataRepository.getBackgroundImages(object :BackgroundImagesCallback{
            override fun onBackgroundImagesLoaded(images: List<String>) {
                _backgroundImageList.postValue(images)
            }

            override fun onBackgroundImagesError() {
                _backgroundImageList.postValue(null)
            }
        })
    }

    // THIS FUNCTION WILL CALL THE LOGO IMAGE LIST FROM DATA REPOSITORY
    fun callLogoImages(){
        DataRepository.getLogoImages(object :LogoImagesCallback{
            override fun onLogoImagesLoaded(images: List<String>) {
                _logoImageList.postValue(images)
            }

            override fun onLogoImagesError() {
                _logoImageList.postValue(null)
            }
        })
    }

    // THIS FUNCTION WILL CALL THE FONT LIST FROM DATA REPOSITORY
    fun callFontList(){
        DataRepository.getFontList(object :FontsCallback{
            override fun onFontsLoaded(fonts: List<Fonts>) {
                _fontList.postValue(fonts)
            }

            override fun onFontsError() {
                _fontList.postValue(null)
            }
        })
    }

}