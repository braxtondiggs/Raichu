package com.cymbit.plastr.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.cymbit.plastr.R

class SettingsFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Inflate the layout for this fragment

        setPreferencesFromResource(R.xml.settings, rootKey)
    }

}