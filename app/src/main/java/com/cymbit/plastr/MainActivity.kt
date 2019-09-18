package com.cymbit.plastr

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.cymbit.plastr.adapter.ViewPagerAdapter
import com.cymbit.plastr.fragment.ExploreFragment
import com.cymbit.plastr.fragment.FavoriteFragment
import com.cymbit.plastr.fragment.SettingsFragment
import com.cymbit.plastr.helpers.Preferences
import com.cymbit.plastr.service.RedditViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.setIconicsFactory
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    var tabNames = listOf("Explore", "Favorites", "Settings")
    private lateinit var redditViewModel: RedditViewModel
    private lateinit var search: SearchView
    private var query: String = ""
    private var menuSort: String = "hot"
    private var menuTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.setIconicsFactory(delegate)
        configureCrashReporting()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        fab.setImageDrawable(
            IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_add).colorRes(R.color.textColorPrimary).size(
                IconicsSize.dp(4)
            )
        )
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ExploreFragment())
        adapter.addFragment(FavoriteFragment())
        adapter.addFragment(SettingsFragment())
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2
        tabs.setupWithViewPager(viewPager)

        tabs.getTabAt(0)?.icon =
            IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_explore)
                .colorRes(R.color.textColorPrimary)
        tabs.getTabAt(1)?.icon =
            IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_favorite)
                .colorRes(R.color.textColorPrimary)
        tabs.getTabAt(2)?.icon =
            IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_settings)
                .colorRes(R.color.textColorPrimary)

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(p0: TabLayout.Tab) {
                toolbar.title = tabNames[p0.position]
                if (::search.isInitialized) {
                    if (!search.isIconified && p0.position == 0) fab.show()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                fab.hide()
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        fab.setOnClickListener {
            val container = main_container
            val searchView = search
            if (query.isNotEmpty()) {
                MaterialDialog(it.context).show {
                    title(text = getString(R.string.app_name) + " - " + query)
                    message(text = getString(R.string.confirm_add) + " " + query + "?")
                    negativeButton { R.string.cancel }
                    positiveButton(R.string.ok) { md ->
                        val subs = Preferences().getSelectedSubs(md.context)
                        subs.add(query)
                        Preferences().setSub(context, query)
                        Preferences().setSelectedSubs(context, subs)
                        searchView.onActionViewCollapsed()
                        Snackbar.make(
                            container,
                            getString(R.string.save_success),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        reset()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        redditViewModel = ViewModelProviders.of(this).get(RedditViewModel::class.java)

        val searchItem = menu.findItem(R.id.action_search)
        searchItem.icon = IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_search)
            .colorRes(R.color.textColorPrimary)
            .size(IconicsSize.dp(18))
        search = searchItem.actionView as SearchView

        search.setIconifiedByDefault(true)
        search.queryHint = "Search"
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            @SuppressLint("DefaultLocale")
            var lastText: String? = null
            override fun onQueryTextSubmit(_query: String): Boolean {
                query = _query.toLowerCase(Locale.US).capitalize()
                viewPager.currentItem = 0
                fab.show()
                redditViewModel.clearData()
                redditViewModel.fetchData(query,  menuSort, menuTime,"", applicationContext, true)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (lastText != null && lastText!!.length > 1 && newText.isEmpty()) reset()
                lastText = newText
                return true
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_best,
            R.id.sort_hot,
            R.id.sort_new,
            R.id.sort_rising -> {
                menuTime = ""
                menuSort = item.title.toString().toUpperCase(Locale.US)
                toolbar.subtitle = menuSort
                redditViewModel.fetchData(Preferences().getSelectedSubs(this).joinToString("+"), menuSort.toLowerCase(Locale.US), menuTime,"", this)
            }
            R.id.sort_controversial,
            R.id.sort_top -> {
                menuSort = item.title.toString().toUpperCase(Locale.US)
            }
            R.id.sort_top_hour,
            R.id.sort_top_day,
            R.id.sort_top_week,
            R.id.sort_top_month,
            R.id.sort_top_year,
            R.id.sort_top_all,
            R.id.sort_controversial_hour,
            R.id.sort_controversial_day,
            R.id.sort_controversial_week,
            R.id.sort_controversial_month,
            R.id.sort_controversial_year,
            R.id.sort_controversial_all -> {
                menuTime = item.title.toString().toUpperCase(Locale.US)
                toolbar.subtitle = "$menuSort:${menuTime}"
                redditViewModel.fetchData(Preferences().getSelectedSubs(this).joinToString("+"), menuSort.toLowerCase(Locale.US), menuTime.toLowerCase(Locale.US),"", this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun reset() {
        fab.hide()
        redditViewModel.clearData()
        redditViewModel.fetchData(
            Preferences().getSelectedSubs(this).joinToString("+"),
            menuSort,
            menuTime,
            "",
            this
        )
    }

    override fun onBackPressed() {
        if (!search.isIconified) {
            search.onActionViewCollapsed()
        } else {
            super.onBackPressed()
        }
    }

    private fun configureCrashReporting() {
        Fabric.with(
            this,
            Crashlytics.Builder().core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build()
        )
    }
}
