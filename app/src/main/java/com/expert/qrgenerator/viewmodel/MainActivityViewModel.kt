package com.expert.qrgenerator.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.repository.DataRepository

class MainActivityViewModel : ViewModel() {

    private var colorList = MutableLiveData<List<String>>()
    private var backgroundImageList = MutableLiveData<List<String>>()
    private var logoImageList = MutableLiveData<List<String>>()
    private var fontList = MutableLiveData<List<Fonts>>()

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
    fun callBackgroundImages(context: Context){
         backgroundImageList = DataRepository.getInstance(context).getBackgroundImages()
    }

    // THIS FUNCTION WILL RETURN THE IMAGE LIST
    fun getBackgroundImages():LiveData<List<String>>{
        return backgroundImageList
    }

    // THIS FUNCTION WILL CALL THE LOGO IMAGE LIST FROM DATA REPOSITORY
    fun callLogoImages(context: Context){
        logoImageList = DataRepository.getInstance(context).getLogoImages()
    }

    // THIS FUNCTION WILL RETURN THE LOGO LIST
    fun getLogoImages():LiveData<List<String>>{
        return logoImageList
    }

    // THIS FUNCTION WILL CALL THE FONT LIST FROM DATA REPOSITORY
    fun callFontList(context: Context){
       fontList = DataRepository.getInstance(context).getFontList()
    }

    // THIS FUNCTION WILL RETURN THE FONT LIST
    fun getFontList():LiveData<List<Fonts>>{
        return fontList
    }

}