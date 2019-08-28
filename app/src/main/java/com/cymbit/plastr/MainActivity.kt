package com.cymbit.plastr

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.cymbit.plastr.adapter.ViewPagerAdapter
import com.cymbit.plastr.fragment.ExploreFragment
import com.cymbit.plastr.fragment.FavoriteFragment
import com.cymbit.plastr.fragment.SettingsFragment
import com.cymbit.plastr.helpers.Preferences
import com.cymbit.plastr.service.RedditViewModel
import com.google.android.material.tabs.TabLayout
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.setIconicsFactory
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class MainActivity : AppCompatActivity() {
    var tabNames = listOf("Explore", "Favorites", "Settings")
    private lateinit var redditViewModel: RedditViewModel
    private lateinit var search: SearchView
    private lateinit var query: String

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.setIconicsFactory(delegate)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        toolbar.navigationIcon =
            IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_tag_faces).colorRes(R.color.textColorPrimary)
                .size(IconicsSize.dp(28))
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
            IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_explore).colorRes(R.color.textColorPrimary)
        tabs.getTabAt(1)?.icon =
            IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_favorite).colorRes(R.color.textColorPrimary)
        tabs.getTabAt(2)?.icon =
            IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_settings).colorRes(R.color.textColorPrimary)

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(p0: TabLayout.Tab?) {
                toolbar.title = tabNames[p0!!.position]
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        fab.onClick {
            MaterialDialog(applicationContext).show {
                title(text = getString(R.string.app_name)  + " - " + query.capitalize())
                message(text = getString(R.string.confirm_add)  + " " + query.capitalize() + "?")
                // positiveButton(R.string.ok) { finish() }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        redditViewModel = ViewModelProviders.of(this).get(RedditViewModel::class.java)

        val searchItem = menu.findItem(R.id.action_search)
        searchItem.icon = IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_search).colorRes(R.color.textColorPrimary)
            .size(IconicsSize.dp(18))
        search = searchItem.actionView as SearchView

        search.setIconifiedByDefault(true)
        search.queryHint = "Search"
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(_query: String): Boolean {
                query = _query
                viewPager.currentItem = 0
                fab.show()
                redditViewModel.clearData()
                redditViewModel.fetchData(query, "")
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }

        })

        search.setOnCloseListener {
            fab.hide()
            redditViewModel.clearData()
            redditViewModel.fetchData(Preferences().getSubs(applicationContext).joinToString("+"), "")
            false
        }
        return true
    }

    override fun onBackPressed() {
        if (!search.isIconified) {
            search.onActionViewCollapsed()
        } else {
            super.onBackPressed()
        }
    }
}
