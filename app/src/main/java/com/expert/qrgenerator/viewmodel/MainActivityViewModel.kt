package com.expert.qrgenerator.viewmodel

import JSONResponse
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.R
import com.expert.qrgenerator.model.Fonts
import com.expert.qrgenerator.repository.DataRepository
import com.expert.qrgenerator.retrofit.ApiRepository
import com.google.gson.JsonObject

class MainActivityViewModel : ViewModel() {

    private var colorList = MutableLiveData<List<String>>()
    private var backgroundImageList = MutableLiveData<List<String>>()
    private var logoImageList = MutableLiveData<List<String>>()
    private var fontList = MutableLiveData<List<Fonts>>()
    private var dynamicQrCodeResponse = MutableLiveData<JsonObject>()
    private var signUpResponse = MutableLiveData<JsonObject>()
    private var signInResponse = MutableLiveData<JsonObject>()

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

    fun createDynamicQrCode(context:Context,body:HashMap<String,String>){
        dynamicQrCodeResponse = ApiRepository.getInstance(context).createDynamicQrCode(body)
    }

    fun getDynamicQrCode():MutableLiveData<JsonObject>{
        return dynamicQrCodeResponse
    }

    fun signUp(context:Context,body:HashMap<String,String>){
        signUpResponse = ApiRepository.getInstance(context).signUp(body)
    }

    fun getSignUp():MutableLiveData<JsonObject>{
        return signUpResponse
    }

    fun signIn(context:Context,email:String){
        signInResponse = ApiRepository.getInstance(context).signIn(email)
    }

    fun getSignIn():MutableLiveData<JsonObject>{
        return signInResponse
    }

}