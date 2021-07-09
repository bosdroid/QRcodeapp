package com.expert.qrgenerator.view.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.utils.AppSettings
import com.expert.qrgenerator.utils.Constants

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        setUpToolbar(this, toolBar, this.getString(R.string.settings))

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            appSettings = AppSettings(requireActivity())

            val soundSwitch = findPreference<SwitchPreferenceCompat>(requireActivity().getString(R.string.key_sound))
            appSettings.putBoolean(requireActivity().getString(R.string.key_sound),soundSwitch!!.isChecked)
            val vibrateSwitch = findPreference<SwitchPreferenceCompat>(requireActivity().getString(R.string.key_vibration))
            appSettings.putBoolean(requireActivity().getString(R.string.key_vibration), vibrateSwitch!!.isChecked)
            val clipboardSwitch = findPreference<SwitchPreferenceCompat>(requireActivity().getString(R.string.key_clipboard))
            appSettings.putBoolean(requireActivity().getString(R.string.key_clipboard), clipboardSwitch!!.isChecked)
            val tipsSwitch = findPreference<SwitchPreferenceCompat>(requireActivity().getString(R.string.key_tips))
            appSettings.putBoolean(requireActivity().getString(R.string.key_tips), tipsSwitch!!.isChecked)
            Constants.tipsValue = tipsSwitch.isChecked
        }
    }
}