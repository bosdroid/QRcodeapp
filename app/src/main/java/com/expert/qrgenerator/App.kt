package com.expert.qrgenerator

import android.app.Application
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application() {

    private lateinit var appSettings: AppSettings

    override fun onCreate() {
        super.onCreate()
        // Initialize OkDownload with custom Dispatcher and singleton instance
        OkDownload.setSingletonInstance(
            OkDownload.Builder(this)
                .downloadDispatcher(DownloadDispatcher())
                .build()
        )

        // Initialize AppSettings instance
        appSettings = AppSettings(applicationContext)

        // Retrieve and set user details if the user is logged in
        getUserDetail()
    }

    /**
     * Checks if the user is logged in and retrieves user details from AppSettings.
     * Updates Constants.userData with the retrieved user information if available.
     */
    private fun getUserDetail() {
        // Check if user is logged in
        if (appSettings.getBoolean(Constants.isLogin)) {
            // Retrieve user details from AppSettings
            val user = appSettings.getUser(Constants.user)
            // Update global user data
            Constants.userData = user
        }
    }
}
