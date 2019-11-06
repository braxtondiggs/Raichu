package com.cymbit.plastr.adapter

import android.annotation.SuppressLint
import android.app.Activity
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
import com.cymbit.plastr.BuildConfig
import com.cymbit.plastr.R

class SettingsAdapter(private var items: MutableList<SettingsItem>) :
    RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.grid_settings, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun getItem(position: Int): SettingsItem {
        return this.items[position]
    }

    class ViewHolder(private val view: View, private val context: Context) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var item: SettingsItem

        override fun onClick(v: View?) {
            if (item.clickable) {
                if (item.id === "share") {
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this wallpaper app called Plastr:\n https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                    (context as Activity).startActivity(Intent.createChooser(shareIntent, "Share App via:"))
                }
            }
        }

        @SuppressLint("ResourceType")
        fun bind(item: SettingsItem) {
            this.item = item
            view.setOnClickListener(this)
            view.image_settings.setImageDrawable(IconicsDrawable(context).icon("gmd_" + item.image).colorRes(R.color.white).size(IconicsSize.dp(18)))
            view.title_settings.text = item.title
            view.summary_settings.text = item.summary

            if (item.clickable) {
                val outValue = TypedValue()
                view.container_settings.isClickable = item.clickable
                context.theme.resolveAttribute(R.attr.selectableItemBackground, outValue, true)
                view.container_settings.setBackgroundResource(outValue.resourceId)
            }
        }
    }
}

data class SettingsItem(val id: String, val title: String, val summary: String, val image: String, val clickable: Boolean)

