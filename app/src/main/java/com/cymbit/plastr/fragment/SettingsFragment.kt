package com.cymbit.plastr.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.cymbit.plastr.R
import com.cymbit.plastr.SubActivity

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val syncSub = findPreference<Preference>("sync_sub")
        syncSub?.setOnPreferenceClickListener { _ ->
            context!!.startActivity(Intent(context, SubActivity::class.java))
            /*MaterialDialog(context!!).show {
                title(R.string.select_sub)
                listItemsMultiChoice(
                    items = Preferences().getAllSubs(context).toList(),
                    initialSelection = Preferences().getSelectedIndices(context)
                ) { _, _, items ->
                    Preferences().setSelectedSubs(context, items)
                }
                negativeButton(R.string.cancel)
                neutralButton(R.string.add_sub) { dialog -> openAddDialog(dialog) }
                positiveButton(R.string.ok)
            }*/
            return@setOnPreferenceClickListener false
        }
    }

    /**/
}