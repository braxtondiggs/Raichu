package com.cymbit.plastr

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.cymbit.plastr.adapter.ViewPagerAdapter
import com.cymbit.plastr.fragment.ExploreFragment
import com.cymbit.plastr.fragment.FavoriteFragment
import com.cymbit.plastr.fragment.SettingsFragment
import com.google.android.material.tabs.TabLayout
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var tabNames = listOf("Explore", "Favorites", "Settings")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_tag_faces).colorRes(R.color.textColorPrimary).size(IconicsSize.dp(28)))
        fab.setImageDrawable(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_add).colorRes(R.color.textColorPrimary).size(IconicsSize.dp(4)))!!
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ExploreFragment())
        adapter.addFragment(FavoriteFragment())
        adapter.addFragment(SettingsFragment())
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)

        tabs.getTabAt(0)?.setIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_explore).colorRes(R.color.textColorPrimary))!!
        tabs.getTabAt(1)?.setIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_favorite).colorRes(R.color.textColorPrimary))!!
        tabs.getTabAt(2)?.setIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_settings).colorRes(R.color.textColorPrimary))!!

        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(p0: TabLayout.Tab?) {
                toolbar.title = tabNames[p0!!.position]
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchItem.setIcon(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_search).colorRes(R.color.textColorPrimary).size(IconicsSize.dp(18)))!!
        val search = searchItem.actionView as SearchView


        search.setIconifiedByDefault(false)
        search.queryHint = "Search"
        search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewPager.setCurrentItem(0)!!
                fab.setVisibility(View.VISIBLE)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }

        })
        return true
    }
}
