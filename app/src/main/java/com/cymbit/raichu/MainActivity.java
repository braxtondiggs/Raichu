package com.cymbit.raichu;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cymbit.raichu.adapter.TabAdapter;
import com.cymbit.raichu.fragment.*;
import com.cymbit.raichu.utils.Preferences;
import com.cymbit.raichu.utils.Utilities;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityIcons;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_view)
    MaterialSearchView searchView;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    List<String> tabNames = Arrays.asList("Explore", "Favorites", "Settings");
    Boolean isSearch = false;
    String mQuery;
    public static Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Context mContext = getApplicationContext();
        super.onCreate(savedInstanceState);
        Iconify.with(new MaterialCommunityModule());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
        bus = new Bus(ThreadEnforcer.MAIN);
        bus.register(this);
        toolbar.setNavigationIcon(new IconDrawable(this, MaterialCommunityIcons.mdi_reddit).colorRes(R.color.textColorPrimary).actionBarSize());
        toolbar.setTitleTextColor(ContextCompat.getColor(mContext, R.color.textColorPrimary));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title(view.getResources().getString(R.string.app_name) + " - " + Utilities.capitalize(mQuery))
                        .content(view.getResources().getString(R.string.confirm_add) + " " + Utilities.capitalize(mQuery) + "?")
                        .positiveText(view.getResources().getString(R.string.ok))
                        .negativeText(view.getResources().getString(R.string.cancel))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Set<String> subs = Preferences.getSubs(MainActivity.this);
                                subs.add(mQuery);
                                Preferences.setSub(MainActivity.this, mQuery);
                                Preferences.setSelectedSub(MainActivity.this, subs);
                            }
                        })
                        .show();
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        TabAdapter adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new ExploreFragment());
        adapter.addFragment(new FavoriteFragment());
        adapter.addFragment(new SettingsFragment());
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
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mQuery = query;
                viewPager.setCurrentItem(0);
                isSearch = true;
                OttoData t = new OttoData();
                t.query = mQuery;
                t.action = "search";
                bus.post(t);
                fab.setVisibility(View.VISIBLE);
                View view = getCurrentFocus();
                if (view != null) {
                    searchView.hideKeyboard(view);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                if (isSearch) {
                    OttoData t = new OttoData();
                    t.query = null;
                    t.action = "search";
                    bus.post(t);
                    isSearch = false;
                    fab.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        setupSearch();
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

    public class OttoData {
        public String query;
        public String action;
    }
}
