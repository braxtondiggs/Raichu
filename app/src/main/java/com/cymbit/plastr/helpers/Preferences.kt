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

    fun getAllSubCharSeq(context: Context): Array<CharSequence?> {
        val list = getAllSubs(context)
        return arrayOfNulls<CharSequence?>(list.size)
    }

    fun setSub(context: Context, sub: String) {
        val editor = getPreferences(context).edit()
        val subs = getAllSubs(context)
        subs.add(sub.capitalize())
        editor.putStringSet("allSubs", subs)
        editor.apply()
    }

    fun getSubs(context: Context): MutableSet<String> {
        return getPreferences(context).getStringSet("selectedSubs", Constants.SELECTED_DEFAULT_SUBS.sorted().toSet())!!
    }

    fun setSelectedSubs(context: Context, subs: MutableSet<String>) {
        val editor = getPreferences(context).edit()
        editor.putStringSet("selectedSubs", subs)
        editor.apply()
    }

    fun getSelectedIndicies(context: Context): IntArray {
        val selected = getSubs(context).toList()
        val subs = getAllSubs(context).toList()

        return selected.intersect(subs).map { subs.indexOf(it) }.toIntArray()
    }
}