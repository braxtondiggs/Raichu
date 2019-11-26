package com.cymbit.plastr.helpers

import android.annotation.SuppressLint
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

    @SuppressLint("DefaultLocale")
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

    fun getNSFW(context: Context): Boolean {
        return getPreferences(context).getBoolean("nsfw", true)
    }

    fun getImageQuality(context: Context): Boolean {
        return getPreferences(context).getBoolean("quality", true)
    }

    fun setPreferenceChange(context: Context, value: Boolean) {
        val editor = getPreferences(context).edit()
        editor.putBoolean("preferenceChange", value)
        editor.apply()
    }

    fun hasPreferenceChange(context: Context): Boolean {
        return getPreferences(context).getBoolean("preferenceChange", false)
    }

    fun getFrequency(context: Context): Int {
        return getPreferences(context).getInt("frequency", 0)
    }

    fun getNetworkPref(context: Context): Int {
        return getPreferences(context).getInt("network", 0)
    }

    fun getNotification(context: Context): Boolean {
        return getPreferences(context).getBoolean("notification", false)
    }

    fun setInt(context: Context, label: String, value: Int) {
         val editor = getPreferences(context).edit()
         editor.putInt(label, value)
         editor.apply()
         setPreferenceChange(context, true)
    }
}