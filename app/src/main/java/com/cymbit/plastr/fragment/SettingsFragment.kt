package com.cymbit.plastr.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cymbit.plastr.BuildConfig
import com.cymbit.plastr.adapter.SettingsAdapter
import com.cymbit.plastr.adapter.SettingsItem
import kotlinx.android.synthetic.main.fragment_settings.*
import android.os.Environment
import com.cymbit.plastr.helpers.Utils
import java.io.File

class SettingsFragment : Fragment() {
    private var folderSize: String = "0 B"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(com.cymbit.plastr.R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val folder = this.getFolderSize(File(Environment.getExternalStorageDirectory().toString() + "/Plastr")).toInt()
        this.folderSize = Utils().humanReadableByteCount(folder, true)
        this.initGridView()
    }

    private fun initGridView() {
        val generalList = mutableListOf(SettingsItem("sync", "Select subreddits", "Choose a source for your wallpapers", "collections", true),
            SettingsItem("quality", "Prefer HD images", "Based on your screen resolution", "hd", true, "checkbox"),
            SettingsItem("quality", "Prefer HD images", "Based on your screen resolution", "hd", true, "checkbox"),
            SettingsItem("nsfw", "Hide NSFW content", "Filter sensitive images", "block", true, "checkbox"),
            SettingsItem("cache", "Clear Cache", "Clear locally cached images", "sd_card", true),
            SettingsItem("directory", "Clear App Directory ", "Clear local app directory", "folder", true))
        rvGeneral.layoutManager = LinearLayoutManager(context)
        rvGeneral.adapter = SettingsAdapter(generalList)

        val autoImageList = mutableListOf(SettingsItem("frequency", "Auto-update frequency", "Off", "timer", true),
            SettingsItem("network", "Preferred Network", "Only used with auto-update", "share", true),
            SettingsItem("rate", "Rate on Google Play", "Tell us what you think about Plastr", "star", true))
        rvAutoImage.layoutManager = LinearLayoutManager(context)
        rvAutoImage.adapter = SettingsAdapter(autoImageList)

        val appList = mutableListOf(SettingsItem("version", "Version", BuildConfig.VERSION_NAME, "code", false),
            SettingsItem("share", "Share ", "Spread the word! Send a link to all your friends", "share", true),
            SettingsItem("rate", "Rate on Google Play", "Tell us what you think about Plastr", "star", true))
        rvApp.layoutManager = LinearLayoutManager(context)
        rvApp.adapter = SettingsAdapter(appList)
    }

    private fun getFolderSize(directory: File? = null): Long {
        var length: Long = 0
        if (directory != null && directory.isDirectory) {
            for (file in directory.listFiles()) {
                length += if (file.isFile) file.length()
                else getFolderSize(file)
            }
        }
        return length
    }
}