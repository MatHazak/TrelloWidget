package com.github.oryanmat.trellowidget.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.annotation.StringRes
import com.github.oryanmat.trellowidget.R
import com.rarepebble.colorpicker.ColorPreference
import com.github.oryanmat.trellowidget.widget.updateWidgets
import com.github.oryanmat.trellowidget.widget.updateWidgetsData
import com.github.oryanmat.trellowidget.util.Constants.T_WIDGET_TAG
import android.util.Log

const val COLOR_FORMAT = "#%08X"

class GeneralPreferenceFragment : PreferenceFragmentCompat() {
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key != null) {
            try {
                setPreferenceChanges(key)
            } catch (e: NullPointerException) {
                Log.e(T_WIDGET_TAG, "Can't find corresponding preference to key $key\n${e.stackTraceToString()}")
            }
        } else {
            Log.e(T_WIDGET_TAG, "Received null key in OnSharedPreferenceChangeListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = preferenceScreen.sharedPreferences
        listener.onSharedPreferenceChanged(preferences, getString(R.string.pref_text_size_key))
        listener.onSharedPreferenceChanged(preferences, getString(R.string.pref_back_color_key))
        listener.onSharedPreferenceChanged(preferences, getString(R.string.pref_fore_color_key))
        listener.onSharedPreferenceChanged(preferences, getString(R.string.pref_title_back_color_key))
        listener.onSharedPreferenceChanged(preferences, getString(R.string.pref_title_fore_color_key))
        listener.onSharedPreferenceChanged(preferences, getString(R.string.pref_title_use_unique_color_key))
        listener.onSharedPreferenceChanged(preferences, getString(R.string.pref_update_interval_key))
        listener.onSharedPreferenceChanged(preferences, getString(R.string.pref_display_board_name_key))
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_general, rootKey)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is ColorPreference)
            preference.showDialog(this, 0)
        else
            super.onDisplayPreferenceDialog(preference)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(listener)
        requireActivity().updateWidgets()
        requireActivity().updateWidgetsData()
    }

    private fun setPreferenceChanges(key: String) {
        when (key) {
            getString(R.string.pref_update_interval_key) -> {
                val preference = findPreference<ListPreference>(key)
                preference?.let {
                    val index = it.findIndexOfValue(it.value)
                    it.summary = String.format(getString(R.string.pref_update_interval_value_desc), it.entries[index])
                }
            }

            getString(R.string.pref_text_size_key) -> {
                val preference = findPreference<ListPreference>(key)
                preference?.let {
                    val index = it.findIndexOfValue(it.value)
                    it.summary = it.entries[index]
                }
            }

            getString(R.string.pref_back_color_key) -> {
                val preference = findPreference<ColorPreference>(key)
                preference?.let {
                    it.summary = String.format(COLOR_FORMAT, it.color)
                }
            }

            getString(R.string.pref_fore_color_key) -> {
                val preference = findPreference<ColorPreference>(key)
                preference?.let {
                    it.summary = String.format(COLOR_FORMAT, it.color)
                }
            }

            getString(R.string.pref_title_back_color_key) -> {
                val preference = findPreference<ColorPreference>(key)
                preference?.let {
                    it.summary = String.format(COLOR_FORMAT, it.color)
                }
            }

            getString(R.string.pref_title_fore_color_key) -> {
                val preference = findPreference<ColorPreference>(key)
                preference?.let {
                    it.summary = String.format(COLOR_FORMAT, it.color)
                }
            }

            getString(R.string.pref_title_use_unique_color_key) -> {
                val preference = findPreference<SwitchPreference>(key)
                preference?.let {
                    it.summary = getString(R.string.pref_title_use_unique_color_desc)
                    colorPreference(R.string.pref_title_fore_color_key)?.isEnabled = it.isChecked
                    colorPreference(R.string.pref_title_back_color_key)?.isEnabled = it.isChecked
                }
            }

            getString(R.string.pref_display_board_name_key) -> {
                val preference = findPreference<SwitchPreference>(key)
                preference?.let {
                    it.summary = getString(R.string.pref_display_board_name_desc)
                }
            }
        }
    }

    private fun colorPreference(@StringRes key: Int): ColorPreference? {
        return findPreference(getString(key))
    }
}