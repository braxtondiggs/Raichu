package com.cymbit.plastr.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.cymbit.plastr.R
import com.cymbit.plastr.helpers.Preferences

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val syncSub = findPreference<Preference>("sync_sub")
        syncSub?.setOnPreferenceClickListener { _ ->
            MaterialDialog(context!!).show {
                title(R.string.select_sub)
                listItemsMultiChoice(items = Preferences().getAllSubs(context).toList(), initialSelection = Preferences().getSelectedIndicies(context))
                negativeButton(R.string.cancel)
                neutralButton(R.string.add_sub) { dialog -> openAddDialog(dialog) }
                // positiveButton(R.string.ok) { dialog -> Preferences().setSelectedSubs(context, Preferences().getSelectedSub(Preferences().getAllSubs(context), dialog.getSes .getSelectedIndices()))}
            }
            return@setOnPreferenceClickListener false
        }
    }

    private fun openAddDialog(dialog: MaterialDialog) {

    }
}