package com.expert.qrgenerator.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.utils.AppSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setUpToolbar(this, toolBar, getString(R.string.settings))

        if (savedInstanceState == null) {
            // Initialize SettingsFragment if not restored
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle toolbar navigation
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var appSettings: AppSettings

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // Load preferences from XML resource
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            appSettings = AppSettings(requireActivity())

            // Initialize switches and their listeners
            initializeSwitchPreference(R.string.key_sound)
            initializeSwitchPreference(R.string.key_vibration)
            initializeSwitchPreference(R.string.key_clipboard)
            initializeSwitchPreference(R.string.key_tips)

            // Initialize list preference and its listener
            val listPreference = findPreference<ListPreference>(getString(R.string.key_mode))
            listPreference?.let {
                setModePreference(it)
                it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    updateModePreference(it, newValue)
                    true
                }
            }
        }

        // Initializes a switch preference with its saved value and change listener
        private fun initializeSwitchPreference(keyResId: Int) {
            val key = getString(keyResId)
            findPreference<SwitchPreferenceCompat>(key)?.apply {
                isChecked = appSettings.getBoolean(key)
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    appSettings.putBoolean(key, newValue as Boolean)
                    true
                }
            }
        }

        // Updates list preference based on new value
        private fun updateModePreference(listPreference: ListPreference, newValue: Any) {
            val type = newValue.toString()
            when (type) {
                getString(R.string.inventory_text) -> {
                    listPreference.value = getString(R.string.inventory_text)
                    listPreference.summary = getString(R.string.inventory_text)
                    appSettings.putString(getString(R.string.key_mode), "0")
                }
                getString(R.string.seller_text) -> {
                    listPreference.value = getString(R.string.seller_text)
                    listPreference.summary = getString(R.string.seller_text)
                    appSettings.putString(getString(R.string.key_mode), "1")
                }
                getString(R.string.quick_links_text) -> {
                    listPreference.value = getString(R.string.quick_links_text)
                    listPreference.summary = getString(R.string.quick_links_text)
                    appSettings.putString(getString(R.string.key_mode), "2")
                }
            }
        }

        // Sets the mode preference summary and value based on saved settings
        private fun setModePreference(listPreference: ListPreference) {
            val mode = appSettings.getString(getString(R.string.key_mode))
            when (mode) {
                "0" -> {
                    listPreference.value = getString(R.string.inventory_text)
                    listPreference.summary = getString(R.string.inventory_text)
                }
                "1" -> {
                    listPreference.value = getString(R.string.seller_text)
                    listPreference.summary = getString(R.string.seller_text)
                }
                "2" -> {
                    listPreference.value = getString(R.string.quick_links_text)
                    listPreference.summary = getString(R.string.quick_links_text)
                }
                else -> {
                    // Default to inventory text if mode is invalid
                    appSettings.putString(getString(R.string.key_mode), "0")
                    listPreference.value = getString(R.string.inventory_text)
                    listPreference.summary = getString(R.string.inventory_text)
                }
            }
        }
    }
}
