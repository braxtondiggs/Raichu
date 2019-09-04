package com.cymbit.plastr

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.cymbit.plastr.adapter.ViewPagerAdapter
import com.cymbit.plastr.fragment.ExploreFragment
import com.cymbit.plastr.fragment.FavoriteFragment
import com.cymbit.plastr.fragment.SettingsFragment
import com.cymbit.plastr.helpers.Preferences
import com.cymbit.plastr.service.FavoriteViewModel
import com.cymbit.plastr.service.RedditFetch
import com.cymbit.plastr.service.RedditViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.setIconicsFactory
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    var tabNames = listOf("Explore", "Favorites", "Settings")
    private lateinit var redditViewModel: RedditViewModel
    private lateinit var search: SearchView
    private lateinit var query: String
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var favorites: List<RedditFetch.RedditChildrenData>

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.setIconicsFactory(delegate)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        toolbar.navigationIcon =
            IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_tag_faces)
                .colorRes(R.color.textColorPrimary)
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
                if (!search.isIconified && p0.position == 0) fab.show()
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
            MaterialDialog(it.context).show {
                title(text = getString(R.string.app_name) + " - " + query)
                message(text = getString(R.string.confirm_add) + " " + query + "?")
                negativeButton { R.string.cancel }
                positiveButton(R.string.ok) {
                    val subs = Preferences().getSelectedSubs(context)
                    subs.add(query)
                    Preferences().setSub(context, query)
                    Preferences().setSelectedSubs(context, subs.toMutableList())
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
        favoriteViewModel = ViewModelProviders.of(this).get(FavoriteViewModel::class.java)
        favoriteViewModel.favoritesLiveData.observe(
            this,
            Observer<List<RedditFetch.RedditChildrenData>> { _favorites ->
                favorites = _favorites
            })
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
            override fun onQueryTextSubmit(_query: String): Boolean {
                query = _query.toLowerCase(Locale("US")).capitalize()
                viewPager.currentItem = 0
                fab.show()
                redditViewModel.clearData()
                redditViewModel.fetchData(query, "", true)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }

        })

        search.setOnCloseListener {
            reset()
            false
        }
        return true
    }

    private fun reset() {
        fab.hide()
        redditViewModel.clearData()
        redditViewModel.fetchData(
            Preferences().getSelectedSubs(applicationContext).joinToString("+"),
            ""
        )
    }

    override fun onBackPressed() {
        if (!search.isIconified) {
            search.onActionViewCollapsed()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val result = data!!.getBooleanExtra("hasFavorite", false)
            val listing: RedditFetch.RedditChildrenData = data.getParcelableExtra("listing")!!
            if (favorites.indexOfFirst { it.id == listing.id } == -1 && result) {
                favoriteViewModel.insert(listing)
            } else if (favorites.indexOfFirst { it.id == listing.id } != -1 && !result) {
                favoriteViewModel.delete(listing)
            }
        }
    }
}
