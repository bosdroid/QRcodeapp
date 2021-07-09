package com.expert.qrgenerator.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.expert.qrgenerator.model.User
import com.google.gson.Gson

class AppSettings(context:Context) {

    private val APP_SHARED_PREFS:String = "qr_magic_prefs"
    private var appSharedPrefs:SharedPreferences
    private var prefsEditor: SharedPreferences.Editor

    init {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS,Activity.MODE_PRIVATE)
        this.prefsEditor = appSharedPrefs.edit() 
    }

    fun getString(key: String): String? {
        return appSharedPrefs.getString(key, "")
    }

    fun getInt(key: String): Int {
        return appSharedPrefs.getInt(key, 0)
    }

    fun getLong(key: String): Long {
        return appSharedPrefs.getLong(key, 0)
    }

    fun getBoolean(key: String): Boolean {
        return if (key == "key_tips") {
            if (appSharedPrefs.contains(key)){
                appSharedPrefs.getBoolean(key, false)
            } else{
                appSharedPrefs.getBoolean(key, true)
            }
        } else{
            appSharedPrefs.getBoolean(key, false)
        }

    }

    fun getUser(key: String): User {
        val value = appSharedPrefs.getString(key, "")
        return Gson().fromJson(value,User::class.java)
    }

    fun putUser(key: String, user: User) {
        val value = Gson().toJson(user)
        prefsEditor.putString(key, value)
        prefsEditor.commit()
    }

    fun putString(key: String, value: String) {
        prefsEditor.putString(key, value)
        prefsEditor.commit()
    }

    fun putInt(key: String, value: Int) {
        prefsEditor.putInt(key, value)
        prefsEditor.commit()
    }

    fun putLong(key: String, value: Long) {
        prefsEditor.putLong(key, value)
        prefsEditor.commit()
    }

    fun putBoolean(key: String, value: Boolean) {
        prefsEditor.putBoolean(key, value)
        prefsEditor.commit()
    }

    fun remove(key: String) {
        prefsEditor.remove(key)
        prefsEditor.commit()
    }

    fun clear() {
        prefsEditor.clear()
        prefsEditor.commit()
    }
}