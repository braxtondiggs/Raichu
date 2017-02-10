package com.cymbit.raichu.utils;

import java.util.Set;
import java.util.TreeSet;

public class Utilities {
    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static Integer[] getSelectedSubInt(Set<String> subs, Set<String> selectedSubs) {
        Set<Integer> selected = new TreeSet<>();
        String[] subArray = subs.toArray(new String[subs.size()]);
        String[] selectedSubArray = selectedSubs.toArray(new String[selectedSubs.size()]);
        for (int i = 0; i < subArray.length; i++) {
            for (int j = 0; j < selectedSubArray.length; j++) {
                if (subArray[i].equals(selectedSubArray[j])) {
                    selected.add(i);
                }
            }
        }
        return selected.toArray(new Integer[selected.size()]);
    }

    public static Set<String> getSelectedSub(Set<String> subs, Integer[] indices) {
        Set<String> selected = new TreeSet<>();
        String[] subArray = subs.toArray(new String[subs.size()]);

        for (Integer indice : indices) {
            selected.add(subArray[indice]);
        }
        return selected;
    }
}
