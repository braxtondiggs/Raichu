package com.cymbit.plastr.fragment

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cymbit.plastr.BuildConfig
import com.cymbit.plastr.R
import com.cymbit.plastr.adapter.SettingsAdapter
import com.cymbit.plastr.adapter.SettingsItem
import com.cymbit.plastr.helpers.Constants
import com.cymbit.plastr.helpers.Preferences
import com.cymbit.plastr.helpers.Utils
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : Fragment() {
    private var folderSize: String = "0 B"
    private var autoImageList: MutableList<SettingsItem> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
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
            SettingsItem("nsfw", "Hide NSFW content", "Filter sensitive images", "block", true, "checkbox"),
            SettingsItem("cache", "Clear Cache", "Clear locally cached images", "sd_card", true),
            SettingsItem("directory", "Clear App Directory ", "Clear local app directory", "folder", true))
        rvGeneral.layoutManager = LinearLayoutManager(context)
        rvGeneral.adapter = SettingsAdapter(generalList) {}

        autoImageList.addAll(getImageView())
        rvAutoImage.layoutManager = LinearLayoutManager(context)
        rvAutoImage.adapter = SettingsAdapter(autoImageList) {
            autoImageList.clear()
            autoImageList.addAll(getImageView())
            rvAutoImage.adapter?.notifyDataSetChanged()
        }

        val appList = mutableListOf(SettingsItem("version", "Version", BuildConfig.VERSION_NAME, "code", false),
            SettingsItem("share", "Share ", "Spread the word! Send a link to all your friends", "share", true),
            SettingsItem("rate", "Rate on Google Play", "Tell us what you think about Plastr", "star", true))
        rvApp.layoutManager = LinearLayoutManager(context)
        rvApp.adapter = SettingsAdapter(appList) {}
    }

    private fun getImageView(): MutableList<SettingsItem> {
        val pref = Preferences()
        val frequency = Constants.FREQUENCY[pref.getFrequency(context!!)]
        val network = Constants.NETWORK[pref.getNetwork(context!!)]
        val screen = getString(resources.getIdentifier(Constants.APPLY[pref.getApplyScreen((context!!))], "string", context?.packageName))
        val time = pref.getBaseTime(context!!)
        val format = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        val isOff = frequency === "Off"
        val list = mutableListOf(SettingsItem("frequency", "Auto-update frequency", frequency, "timer", true),
            SettingsItem("network", "Preferred Network", network, "share", true, "normal", isOff),
            SettingsItem("apply", "Apply images to", screen, "phonelink_setup", true, "normal", isOff),
            SettingsItem("notification", "Notification", "Get notified when new wallpaper is applied", "notifications", true, "checkbox", isOff))
        if (!isOff) list.add(SettingsItem("up_next", "Next wallpaper at ${format.format(time)}", null, "access_time", false))
        return list

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