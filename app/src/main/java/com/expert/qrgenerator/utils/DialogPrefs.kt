package com.expert.qrgenerator.utils

import android.content.Context
import android.content.SharedPreferences

object DialogPrefs {
    private const val prefsName = "_prefs_"
    const val KEY_SUCCESS_SCAN = "KEY_SUCCESS_SCAN"
    const val KEY_SHARE_QR = "KEY_SHARE_QR"
    const val KEY_DAY_PASSED = "KEY_DAY_PASSED"

    fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    fun setDate(context: Context, date: String) {
        getPreferences(context).edit().putString(KEY_DAY_PASSED, date).apply()
    }

    fun getDate(context: Context): String? {
        return getPreferences(context).getString(KEY_DAY_PASSED, null)
    }

    fun setSuccessScan(context: Context, scan: Int) {
        getPreferences(context).edit().putInt(KEY_SUCCESS_SCAN, scan).apply()
    }

    fun getSuccessScan(context: Context): Int {
        return getPreferences(context).getInt(KEY_SUCCESS_SCAN, 0)
    }

    fun setShared(context: Context, isShared: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_SHARE_QR, isShared).apply()
    }

    fun getShared(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_SHARE_QR, false)
    }

    fun clearPreferences(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}