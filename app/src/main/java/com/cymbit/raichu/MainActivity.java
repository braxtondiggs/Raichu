package com.cymbit.raichu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.cymbit.raichu.adapter.TabAdapter;
import com.cymbit.raichu.fragment.*;
import com.cymbit.raichu.utils.preferences.JSONSharedPreferences;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_view)
    MaterialSearchView searchView;
    private SharedPreferences prefs;
    List<String> tabNames = Arrays.asList("Explore", "Favorites", "Settings");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Context mContext = getApplicationContext();
        super.onCreate(savedInstanceState);
        Iconify.with(new MaterialCommunityModule());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
        setupSearch();
        toolbar.setNavigationIcon(new IconDrawable(this, MaterialCommunityIcons.mdi_reddit).colorRes(R.color.textColorPrimary).actionBarSize());
        toolbar.setTitleTextColor(ContextCompat.getColor(mContext, R.color.textColorPrimary));
        prefs = getSharedPreferences("com.cymbit.Raichu", MODE_PRIVATE);
        if (prefs.getBoolean("firstRun", true)) {
            setSettings();
            prefs.edit().putBoolean("firstRun", false).apply();
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new ExploreFragment(), tabNames.get(0));
        adapter.addFragment(new FavoriteFragment(), tabNames.get(1));
        adapter.addFragment(new SettingsFragment(), tabNames.get(2));
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                toolbar.setTitle(tabNames.get(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupTabIcons() {
        TabLayout.Tab tab1 = tabLayout.getTabAt(0);
        TabLayout.Tab tab2 = tabLayout.getTabAt(1);
        TabLayout.Tab tab3 = tabLayout.getTabAt(2);
        if (tab1 != null && tab2 != null && tab3 != null) {
            tab1.setIcon(new IconDrawable(this, MaterialCommunityIcons.mdi_earth).colorRes(R.color.textColorPrimary).actionBarSize());
            tab2.setIcon(new IconDrawable(this, MaterialCommunityIcons.mdi_heart).colorRes(R.color.textColorPrimary).actionBarSize());
            tab3.setIcon(new IconDrawable(this, MaterialCommunityIcons.mdi_settings).colorRes(R.color.textColorPrimary).actionBarSize());
        }

    }

    private void setupSearch() {
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    private void setSettings() {
        JSONArray subs = new JSONArray();
        JSONObject sub = new JSONObject();
        try {
            sub.put("name", "/r/Wallpapers");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        subs.put(sub);
        JSONSharedPreferences.saveJSONArray(this, "cymbit", "subs", subs);

    }
}
