package com.expert.qrgenerator.utils

import android.content.Context
import android.content.SharedPreferences

object DialogPrefs {

    // SharedPreferences file name
    private const val PREFS_NAME = "_prefs_"

    // Keys for different preferences
    private const val KEY_SUCCESS_SCAN = "KEY_SUCCESS_SCAN"
    private const val KEY_SHARE_QR = "KEY_SHARE_QR"
    private const val KEY_DAY_PASSED = "KEY_DAY_PASSED"

    /**
     * Retrieves the SharedPreferences instance.
     *
     * @param context The context to use for accessing SharedPreferences.
     * @return The SharedPreferences instance.
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Saves the given date string to SharedPreferences.
     *
     * @param context The context to use for accessing SharedPreferences.
     * @param date The date string to save.
     */
    fun setDate(context: Context, date: String) {
        getPreferences(context)
            .edit()
            .putString(KEY_DAY_PASSED, date)
            .apply()
    }

    /**
     * Retrieves the saved date string from SharedPreferences.
     *
     * @param context The context to use for accessing SharedPreferences.
     * @return The saved date string, or null if not found.
     */
    fun getDate(context: Context): String? {
        return getPreferences(context).getString(KEY_DAY_PASSED, null)
    }

    /**
     * Saves the number of successful scans to SharedPreferences.
     *
     * @param context The context to use for accessing SharedPreferences.
     * @param scan The number of successful scans to save.
     */
    fun setSuccessScan(context: Context, scan: Int) {
        getPreferences(context)
            .edit()
            .putInt(KEY_SUCCESS_SCAN, scan)
            .apply()
    }

    /**
     * Retrieves the number of successful scans from SharedPreferences.
     *
     * @param context The context to use for accessing SharedPreferences.
     * @return The number of successful scans, defaulting to 0 if not found.
     */
    fun getSuccessScan(context: Context): Int {
        return getPreferences(context).getInt(KEY_SUCCESS_SCAN, 0)
    }

    /**
     * Saves the shared status of the QR code to SharedPreferences.
     *
     * @param context The context to use for accessing SharedPreferences.
     * @param isShared The shared status of the QR code.
     */
    fun setShared(context: Context, isShared: Boolean) {
        getPreferences(context)
            .edit()
            .putBoolean(KEY_SHARE_QR, isShared)
            .apply()
    }

    /**
     * Retrieves the shared status of the QR code from SharedPreferences.
     *
     * @param context The context to use for accessing SharedPreferences.
     * @return True if the QR code has been shared, false otherwise.
     */
    fun getShared(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_SHARE_QR, false)
    }

    /**
     * Clears all preferences from SharedPreferences.
     *
     * @param context The context to use for accessing SharedPreferences.
     */
    fun clearPreferences(context: Context) {
        getPreferences(context)
            .edit()
            .clear()
            .apply()
    }
}
