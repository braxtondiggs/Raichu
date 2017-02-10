package com.cymbit.raichu.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Preferences {
    private static SharedPreferences getPreferences(Activity activity) {
        return PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public static Set<String> getAllSubs(Activity activity) {
        List<String> sortList = new ArrayList<>();
        sortList.addAll(getPreferences(activity).getStringSet("allSubs", new TreeSet<>(Constants.DEFAULT_SUBS)));
        java.util.Collections.sort(sortList);
        return new TreeSet<>(sortList);
    }

    public static CharSequence[] getAllSubCharSeq(Activity activity) {
        List<String> sortList = new ArrayList<>();
        sortList.addAll(getAllSubs(activity));
        java.util.Collections.sort(sortList);
        return sortList.toArray(new CharSequence[sortList.size()]);
    }

    public static void setSub(Activity activity, String sub) {
        SharedPreferences.Editor editor = getPreferences(activity).edit();
        Set<String> subs = getAllSubs(activity);
        subs.add(Utilities.capitalize(sub));
        editor.putStringSet("allSubs", subs);
        editor.apply();
    }

    public static Set<String> getSubs(Activity activity) {
        return getPreferences(activity).getStringSet("selectedSubs", new TreeSet<>(Constants.SELECTED_DEFAULT_SUBS));
    }

    public static void setSelectedSub(Activity activity, Set<String> subs) {
        SharedPreferences.Editor editor = getPreferences(activity).edit();
        editor.putStringSet("selectedSubs", subs);
        editor.apply();
    }
}
