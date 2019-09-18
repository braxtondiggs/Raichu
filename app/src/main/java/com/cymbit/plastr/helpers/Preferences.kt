package com.cymbit.plastr.helpers

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Preferences {
    private fun getPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getAllSubs(context: Context): MutableSet<String> {
        return getPreferences(context).getStringSet("allSubs", Constants.DEFAULT_SUBS.sorted().toSet())!!
    }


    fun setAllSubs(context: Context, items: Set<String>) {
        val editor = getPreferences(context).edit()
        editor.remove("allSubs")
        editor.apply()
        editor.putStringSet("allSubs", items)
        editor.apply()
    }

    fun setSub(context: Context, text: String) {
        val editor = getPreferences(context).edit()
        val subs = getAllSubs(context)
        subs.add(text.capitalize())
        editor.putStringSet("allSubs", subs)
        editor.apply()
    }

    fun getSelectedSubs(context: Context): MutableSet<String> {
        return getPreferences(context).getStringSet("selectedSubs", Constants.SELECTED_DEFAULT_SUBS.sorted().toSet())!!
    }

    fun setSelectedSubs(context: Context, items: Set<String>) {
        val editor = getPreferences(context).edit()
        editor.remove("selectedSubs")
        editor.apply()
        editor.putStringSet("selectedSubs", items)
        editor.apply()
    }

    fun getSelectedIndices(context: Context): IntArray {
        val subs = getAllSubs(context).toList()
        return getSelectedSubs(context).toList().intersect(subs).map { subs.indexOf(it) }.toIntArray()
    }

    fun getNSFW(context: Context): Boolean  {
        return getPreferences(context).getBoolean("nsfw", false)
    }
}