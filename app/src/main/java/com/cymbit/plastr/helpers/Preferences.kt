package com.cymbit.plastr.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.util.*

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

    fun getNetwork(context: Context): Int {
        return getPreferences(context).getInt("network", 0)
    }

    fun getApplyScreen(context: Context): Int {
        return getPreferences(context).getInt("apply", 2)
    }

    fun getNotification(context: Context): Boolean {
        return getPreferences(context).getBoolean("notification", true)
    }

    fun setInt(context: Context, label: String, value: Int) {
        val editor = getPreferences(context).edit()
        editor.putInt(label, value)
        editor.apply()
        setPreferenceChange(context, true)
    }

    fun getImageHistory(context: Context): MutableSet<String> {
        val history = getPreferences(context).getStringSet("history", mutableSetOf())
        return if (!history.isNullOrEmpty()) history else mutableSetOf()
    }

    fun setHistory(context: Context, history: MutableSet<String>, id: String) {
        val editor = getPreferences(context).edit()
        history.add(id)
        val value = history.toList().takeLast(24).toSet()
        editor.putStringSet("history", value)
        editor.apply()
    }

    fun getBaseTime(context: Context): Date {
        val cal = Calendar.getInstance()
        cal.timeInMillis = getPreferences(context).getLong("nextTime", 0L)
        return cal.time
    }

    fun setBaseTime(context: Context, date: Date) {
        val editor = getPreferences(context).edit()
        editor.putLong("nextTime", date.time)
        editor.apply()
    }

    fun getSort(context: Context): String? {
        return getPreferences(context).getString("sort", "hot")
    }

    fun getTime(context: Context): String? {
        return getPreferences(context).getString("time", "")
    }

    fun setString(context: Context, label: String, value: String?) {
        val editor = getPreferences(context).edit()
        editor.putString(label, value)
        editor.apply()
    }
}