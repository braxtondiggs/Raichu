package com.cymbit.plastr.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cymbit.plastr.BuildConfig
import com.cymbit.plastr.R
import com.cymbit.plastr.adapter.SettingsAdapter
import com.cymbit.plastr.adapter.SettingsItem
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment() {
    private lateinit var mGridAdapter: SettingsAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initGridView()

        /*val syncSub = findPreference<Preference>("sync_sub")
        syncSub?.setOnPreferenceClickListener { _ ->
            context!!.startActivity(Intent(context, SubActivity::class.java))
            return@setOnPreferenceClickListener false
        }*/
    }

    private fun initGridView() {
        val appList = mutableListOf(
            SettingsItem("version", "Version", BuildConfig.VERSION_NAME, "code", false),
            SettingsItem("share", "Share ", "Spread the word! Send a link to all your friends", "share", true),
            SettingsItem("rate", "Rate on Google Play", "Tell us what you think about Plastr", "star", true))
        rvApp.layoutManager = LinearLayoutManager(context)
        rvApp.adapter = SettingsAdapter(appList)
    }
}