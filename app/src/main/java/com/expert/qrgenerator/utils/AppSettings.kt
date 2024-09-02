package com.expert.qrgenerator.utils

import android.content.Context
import android.content.SharedPreferences
import com.expert.qrgenerator.model.User
import com.google.gson.Gson

/**
 * A utility class for managing app settings using SharedPreferences.
 * Provides methods to get and set various types of data.
 */
class AppSettings(context: Context) {

    private val appSharedPrefsName = "qr_magic_prefs"
    private val appSharedPrefs: SharedPreferences = context.getSharedPreferences(appSharedPrefsName, Context.MODE_PRIVATE)
    private val prefsEditor: SharedPreferences.Editor = appSharedPrefs.edit()

    /**
     * Retrieves a String value from SharedPreferences.
     *
     * @param key The key for the value.
     * @return The String value associated with the key, or an empty String if not found.
     */
    fun getString(key: String): String? = appSharedPrefs.getString(key, "")

    /**
     * Retrieves an Int value from SharedPreferences.
     *
     * @param key The key for the value.
     * @return The Int value associated with the key, or 0 if not found.
     */
    fun getInt(key: String): Int = appSharedPrefs.getInt(key, 0)

    /**
     * Retrieves a Long value from SharedPreferences.
     *
     * @param key The key for the value.
     * @return The Long value associated with the key, or 0 if not found.
     */
    fun getLong(key: String): Long = appSharedPrefs.getLong(key, 0)

    /**
     * Retrieves a Boolean value from SharedPreferences with a default behavior
     * for specific keys related to app preferences.
     *
     * @param key The key for the value.
     * @return The Boolean value associated with the key. Default is true for specified keys.
     */
    fun getBoolean(key: String): Boolean {
        val default = when (key) {
            "key_tips", "key_sound", "key_vibration", "key_clipboard" -> true
            else -> false
        }
        return appSharedPrefs.getBoolean(key, default)
    }

    /**
     * Retrieves a User object from SharedPreferences.
     *
     * @param key The key for the value.
     * @return The User object associated with the key.
     */
    fun getUser(key: String): User {
        val value = appSharedPrefs.getString(key, null) ?: return User("","","","","","")
        return Gson().fromJson(value, User::class.java)
    }

    /**
     * Saves a User object to SharedPreferences.
     *
     * @param key The key for the value.
     * @param user The User object to be saved.
     */
    fun putUser(key: String, user: User) {
        val value = Gson().toJson(user)
        prefsEditor.putString(key, value).apply()
    }

    /**
     * Saves a String value to SharedPreferences.
     *
     * @param key The key for the value.
     * @param value The String value to be saved.
     */
    fun putString(key: String, value: String) {
        prefsEditor.putString(key, value).apply()
    }

    /**
     * Saves an Int value to SharedPreferences.
     *
     * @param key The key for the value.
     * @param value The Int value to be saved.
     */
    fun putInt(key: String, value: Int) {
        prefsEditor.putInt(key, value).apply()
    }

    /**
     * Saves a Long value to SharedPreferences.
     *
     * @param key The key for the value.
     * @param value The Long value to be saved.
     */
    fun putLong(key: String, value: Long) {
        prefsEditor.putLong(key, value).apply()
    }

    /**
     * Saves a Boolean value to SharedPreferences.
     *
     * @param key The key for the value.
     * @param value The Boolean value to be saved.
     */
    fun putBoolean(key: String, value: Boolean) {
        prefsEditor.putBoolean(key, value).apply()
    }

    /**
     * Removes a value from SharedPreferences.
     *
     * @param key The key for the value to be removed.
     */
    fun remove(key: String) {
        prefsEditor.remove(key).apply()
    }

    /**
     * Clears all values from SharedPreferences.
     */
    fun clear() {
        prefsEditor.clear().apply()
    }
}
