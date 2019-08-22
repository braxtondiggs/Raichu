package com.cymbit.plastr.fragment

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.checkItems
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.updateListItemsMultiChoice
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
                listItemsMultiChoice(
                    items = Preferences().getAllSubs(context).toList(),
                    initialSelection = Preferences().getSelectedIndices(context)
                ) { _, _, items ->
                    Preferences().setSelectedSubs(context, items)
                }
                negativeButton(R.string.cancel)
                neutralButton(R.string.add_sub) { dialog -> openAddDialog(dialog) }
                positiveButton(R.string.ok)
            }
            return@setOnPreferenceClickListener false
        }
    }

    private fun openAddDialog(dialog: MaterialDialog) {
        MaterialDialog(context!!).show {
            title(R.string.add_sub)
            message(R.string.add_sub_description)
            input(maxLength = 32, waitForPositiveButton = true) { _, text ->
                Preferences().setSub(context, text.toString())
            }
            positiveButton(R.string.ok)
            negativeButton(R.string.cancel)
            onDismiss {
                dialog.updateListItemsMultiChoice(items = Preferences().getAllSubs(context).toList())
                dialog.checkItems(Preferences().getSelectedIndices(context))
                dialog.show()
            }
        }
    }
}