package com.expert.qrgenerator.view.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.utils.AppSettings

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
            soundSwitch!!.isChecked  = appSettings.getBoolean(requireActivity().getString(R.string.key_sound))
            soundSwitch.setOnPreferenceChangeListener(object :
                Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    appSettings.putBoolean(
                        requireActivity().getString(R.string.key_sound),
                        newValue as Boolean
                    )
                    return true
                }
            })
            val vibrateSwitch = findPreference<SwitchPreferenceCompat>(requireActivity().getString(R.string.key_vibration))
            vibrateSwitch!!.isChecked = appSettings.getBoolean(requireActivity().getString(R.string.key_vibration))
            vibrateSwitch.setOnPreferenceChangeListener(object :
                Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    appSettings.putBoolean(
                        requireActivity().getString(R.string.key_vibration),
                        newValue as Boolean
                    )
                    return true
                }
            })
            val clipboardSwitch = findPreference<SwitchPreferenceCompat>(
                requireActivity().getString(
                    R.string.key_clipboard
                )
            )
            clipboardSwitch!!.isChecked = appSettings.getBoolean(requireActivity().getString(R.string.key_clipboard))
            clipboardSwitch.setOnPreferenceChangeListener(object :
                Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    appSettings.putBoolean(
                        requireActivity().getString(R.string.key_clipboard),
                        newValue as Boolean
                    )
                    return true
                }
            })


            val tipsSwitch = findPreference<SwitchPreferenceCompat>(requireActivity().getString(R.string.key_tips))
            tipsSwitch!!.isChecked = appSettings.getBoolean(requireActivity().getString(R.string.key_tips))
            tipsSwitch.setOnPreferenceChangeListener(object :
                Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                    appSettings.putBoolean(
                        requireActivity().getString(R.string.key_tips),
                        newValue as Boolean
                    )
                    return true
                }
            })

            val listPreference = findPreference<ListPreference>(getString(R.string.key_mode))
            getModePreference(listPreference)
            listPreference!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    val type = newValue.toString()
                    when (type) {
                        getString(R.string.inventory_text) -> {
                            listPreference.value = getString(R.string.inventory_text)
                            listPreference.summary = getString(R.string.inventory_text)
                            appSettings.putString(getString(R.string.key_mode),"0")
                        }
                        getString(R.string.seller_text) -> {
                            listPreference.value = getString(R.string.seller_text)
                            listPreference.summary = getString(R.string.seller_text)
                            appSettings.putString(getString(R.string.key_mode),"1")
                        }
                        getString(R.string.quick_links_text) -> {
                            listPreference.value = getString(R.string.quick_links_text)
                            listPreference.summary = getString(R.string.quick_links_text)
                            appSettings.putString(getString(R.string.key_mode),"2")
                        }
                    }
                    false
                }

        }

        private fun getModePreference(listPreference: ListPreference?) {
            when(appSettings.getString(getString(R.string.key_mode))){
                "0" -> {
                    listPreference!!.value = getString(R.string.inventory_text)
                    listPreference.summary = getString(R.string.inventory_text)

                }
                "1" -> {
                    listPreference!!.value = getString(R.string.seller_text)
                    listPreference.summary = getString(R.string.seller_text)
                }
                "2" -> {
                    listPreference!!.value = getString(R.string.quick_links_text)
                    listPreference.summary = getString(R.string.quick_links_text)
                }
                else->{
                    appSettings.putString(getString(R.string.key_mode),"0")
                    listPreference!!.value = getString(R.string.inventory_text)
                    listPreference.summary = getString(R.string.inventory_text)
                }
            }
        }
    }
}