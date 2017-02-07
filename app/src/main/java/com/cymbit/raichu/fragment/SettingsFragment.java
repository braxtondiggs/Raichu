package com.cymbit.raichu.fragment;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.cymbit.raichu.R;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SettingsFragment extends PreferenceFragmentCompat {
    CheckBoxPreference mCycle;
    CheckBoxPreference mWifi;
    CheckBoxPreference mNSFW;
    CheckBoxPreference mNotify;
    MultiSelectListPreference mSub;
    ListPreference mSync;
    Preference mVersion;
    List<String> entities = new ArrayList<>();

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        mCycle = (CheckBoxPreference) getPreferenceManager().findPreference("perform_cycle");
        mWifi = (CheckBoxPreference) getPreferenceManager().findPreference("perform_wifi");
        mNotify = (CheckBoxPreference) getPreferenceManager().findPreference("perform_alert");
        mNSFW = (CheckBoxPreference) getPreferenceManager().findPreference("perform_nsfw");
        mVersion = getPreferenceManager().findPreference("version");
        mSync = (ListPreference) getPreferenceManager().findPreference("sync_interval");
        mSub = (MultiSelectListPreference) getPreferenceManager().findPreference("sync_sub");

        mCycle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                toggleCycle(Boolean.valueOf(value.toString()));
                return true;
            }
        });
        toggleCycle(mCycle.isChecked());
        addSubs();
        final CharSequence[] charSequenceItems = entities.toArray(new CharSequence[entities.size()]);
        mSub.setEntries(charSequenceItems);
        mSub.setEntryValues(charSequenceItems);
        mSub.setDefaultValue(charSequenceItems);
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            mVersion.setSummary(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {

    }

    private void toggleCycle(Boolean status) {
        mWifi.setEnabled(status);
        mSync.setEnabled(status);
    }

    private void addSubs() {
        entities.addAll(Arrays.asList("EarthPorn", "SpacePorn", "APodStream", "WindowShots", "Wallpapers", "ITookAPicture", "AlbumArtPorn", "MusicWallpapers", "ConcertPorn", "ExposurePorn", "SkyPorn", "FractalPorn", "ImaginaryTechnology", "BridgePorn", "RedWall"));

    }
}