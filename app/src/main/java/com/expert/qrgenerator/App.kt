package com.expert.qrgenerator

import android.app.Application
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants

class App : Application() {

    private lateinit var appSettings: AppSettings
    override fun onCreate() {
        super.onCreate()
        appSettings = AppSettings(applicationContext)
        getUserDetail()
    }


    private fun getUserDetail(){
        if (appSettings.getString(Constants.isLogin) == "true"){
            val user = appSettings.getUser(Constants.user)
            Constants.userData = user
        }
    }

}