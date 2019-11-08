package com.cymbit.plastr.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize
import com.mikepenz.iconics.utils.colorRes
import kotlinx.android.synthetic.main.grid_settings.view.*
import android.util.TypedValue
import android.content.Intent
import android.net.Uri
import android.os.Environment
import com.bumptech.glide.Glide
import com.cymbit.plastr.BuildConfig
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import java.io.File
import android.os.AsyncTask
import androidx.preference.PreferenceManager
import com.cymbit.plastr.SubActivity
import com.cymbit.plastr.helpers.Preferences


class SettingsAdapter(private var items: MutableList<SettingsItem>) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(com.cymbit.plastr.R.layout.grid_settings, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun getItem(position: Int): SettingsItem {
        return this.items[position]
    }

    class ViewHolder(private val view: View, private val context: Context) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var item: SettingsItem

        override fun onClick(v: View?) {
            if (item.clickable) {
                when {
                    item.id === "cache" -> {
                        context.cacheDir.deleteRecursively()
                        CacheClearAsyncTask(Glide.get(context)).execute()
                        Snackbar.make(view, "Cache cleared successfully", Snackbar.LENGTH_SHORT).show()
                    }
                    item.id === "directory" -> {
                        doAsync {
                            @Suppress("DEPRECATION") File(Environment.getExternalStorageDirectory().toString() + "/Plastr").deleteRecursively()
                            onComplete {
                                Snackbar.make(view, "Directory cleared successfully", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                    item.id === "rate" -> try {
                        val uri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
                        (context as Activity).startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (e: ActivityNotFoundException) {
                        Snackbar.make(view, "Unable to find market app", Snackbar.LENGTH_SHORT).show()
                    }
                    item.id === "share" -> {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(com.cymbit.plastr.R.string.app_name))
                        shareIntent.putExtra(Intent.EXTRA_TEXT,
                            "Check out this wallpaper app called Plastr:\n https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                        (context as Activity).startActivity(Intent.createChooser(shareIntent, "Share App via:"))
                    }
                    item.id === "sync" -> {
                        context.startActivity(Intent(context, SubActivity::class.java))
                    }
                    item.type === "checkbox" -> {
                        view.checkBox.isChecked = !view.checkBox.isChecked
                    }
                }
            }
        }

        @SuppressLint("ResourceType")
        fun bind(item: SettingsItem) {
            this.item = item
             val pref = PreferenceManager.getDefaultSharedPreferences(context)
            view.setOnClickListener(this)
            view.image_settings.setImageDrawable(
                IconicsDrawable(context).icon("gmd_" + item.image).colorRes(com.cymbit.plastr.R.color.white).size(IconicsSize.dp(18)))
            view.title_settings.text = item.title
            view.summary_settings.text = item.summary

            if (item.clickable) {
                val outValue = TypedValue()
                view.container_settings.isClickable = item.clickable
                context.theme.resolveAttribute(com.cymbit.plastr.R.attr.selectableItemBackground, outValue, true)
                view.container_settings.setBackgroundResource(outValue.resourceId)
            }
            if (item.type === "checkbox") {
                view.checkBox.visibility = View.VISIBLE
                val checkedValue = pref.getBoolean(item.id, true)
                view.checkBox.isChecked = checkedValue
            }

            view.checkBox.setOnCheckedChangeListener { _, isChecked ->
                val editor = pref.edit()
                editor.putBoolean(item.id, isChecked)
                editor.apply()
                Preferences().setPreferenceChange(context, true)
            }
        }
    }
}

data class SettingsItem(val id: String, val title: String, val summary: String, val image: String, val clickable: Boolean, val type: String = "normal")

internal class CacheClearAsyncTask(private var glide: Glide) : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg params: Void?): Void? {
        glide.clearDiskCache()
        return null
    }

    override fun onPostExecute(result: Void?) {
        glide.clearMemory()
    }
}

