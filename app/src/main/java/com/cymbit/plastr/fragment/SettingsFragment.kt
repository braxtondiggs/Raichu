package com.cymbit.plastr.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.cymbit.plastr.R
import com.cymbit.plastr.SubActivity

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let { getColor(it, R.color.midnightBlue) }?.let { view.setBackgroundColor(it) }

        val syncSub = findPreference<Preference>("sync_sub")
        syncSub?.setOnPreferenceClickListener { _ ->
            context!!.startActivity(Intent(context, SubActivity::class.java))
            return@setOnPreferenceClickListener false
        }
    }
}