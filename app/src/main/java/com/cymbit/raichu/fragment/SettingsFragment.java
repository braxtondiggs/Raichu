package com.cymbit.raichu.fragment;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.text.InputType;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cymbit.raichu.R;
import com.cymbit.raichu.utils.Preferences;
import com.cymbit.raichu.utils.Utilities;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import java.util.Set;


public class SettingsFragment extends PreferenceFragmentCompat {
    CheckBoxPreference mCycle;
    CheckBoxPreference mWifi;
    CheckBoxPreference mNSFW;
    CheckBoxPreference mNotify;
    Preference mSub;
    ListPreference mSync;
    Preference mVersion;

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
        mSub = getPreferenceManager().findPreference("sync_sub");

        mCycle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                toggleCycle(Boolean.valueOf(value.toString()));
                return true;
            }
        });
        toggleCycle(mCycle.isChecked());
        mSub.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                        .title("Select Subreddits")
                        .items(Preferences.getAllSubCharSeq(getActivity()))
                        .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                return true;
                            }
                        })
                        .positiveText("OK")
                        .negativeText("Cancel")
                        .neutralText("Add Subreddit")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Preferences.setSelectedSub(getActivity(), Utilities.getSelectedSub(Preferences.getAllSubs(getActivity()), dialog.getSelectedIndices()));
                                //ExploreFragment.update();
                            }
                        })
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                //dialog.hide();
                                openAddDialog(dialog);
                            }
                        }).show();
                dialog.setSelectedIndices(Utilities.getSelectedSubInt(Preferences.getAllSubs(getActivity()), Preferences.getSubs(getActivity())));
                return false;
            }
        });
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
        if (!status) {
            mWifi.setChecked(false);
        }
    }

    private void openAddDialog(final MaterialDialog _dialog) {
        new MaterialDialog.Builder(getContext())
                .title("Add Subreddit")
                .content("Enter subreddit of your choice")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(3, 30)
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                    }
                })
                .negativeText("Cancel")
                .positiveText("OK")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String sub = dialog.getInputEditText().getText().toString();
                        Set<String> subs = Preferences.getSubs(getActivity());
                        subs.add(sub);
                        Preferences.setSub(getActivity(), sub);
                        Preferences.setSelectedSub(getActivity(), subs);
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        _dialog.setItems(Preferences.getAllSubCharSeq(getActivity()));
                        _dialog.setSelectedIndices(Utilities.getSelectedSubInt(Preferences.getAllSubs(getActivity()), Preferences.getSubs(getActivity())));
                        _dialog.show();
                    }
                }).show();
    }
}