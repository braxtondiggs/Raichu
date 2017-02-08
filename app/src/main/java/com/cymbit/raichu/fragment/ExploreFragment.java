package com.cymbit.raichu.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cymbit.raichu.R;
import com.cymbit.raichu.adapter.ExploreAdapter;
import com.cymbit.raichu.api.RedditAPIClient;
import com.cymbit.raichu.model.Favorites;
import com.cymbit.raichu.model.Listing;
import com.cymbit.raichu.model.ListingData;
import com.cymbit.raichu.model.ListingsResponse;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.grid.BasicGridLayoutManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ExploreFragment extends Fragment {
    private ListingsResponse mListings;
    @BindView(R.id.explore_recycler)
    UltimateRecyclerView recyclerView;
    @BindView(R.id.loading)
    SmoothProgressBar loading;
    @BindView(R.id.loadingCircle)
    ProgressBar loadingCircle;

    private Unbinder unbinder;
    private BasicGridLayoutManager mGridLayoutManager;
    @SuppressLint("StaticFieldLeak")
    static ExploreAdapter mGridAdapter = null;
    SharedPreferences preferences;

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Iconify.with(new MaterialCommunityModule());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_explore, container, false);
        unbinder = ButterKnife.bind(this, view);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        loadData(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void loadData(final View view) {
        loading.progressiveStart();
        loadingCircle.setVisibility(View.VISIBLE);
        if (isNetworkAvailable()) {
            RedditAPIClient.getListings(concatSubs(getSubs()), (mListings != null) ? mListings.getData().getAfter() : null).enqueue(new Callback<ListingsResponse>() {
                @Override
                public void onResponse(Call<ListingsResponse> call, final Response<ListingsResponse> response) {
                    loading.progressiveStop();
                    loadingCircle.setVisibility(View.GONE);
                    if (response.code() == 200) {
                        mListings = response.body();
                        mGridAdapter = new ExploreAdapter(filter(response.body().getData().getChildren()), Favorites.listAll(Favorites.class));
                        mGridAdapter.setSpanColumns(2);
                        mGridLayoutManager = new BasicGridLayoutManager(view.getContext(), 2, mGridAdapter);

                        recyclerView.setLayoutManager(mGridLayoutManager);
                        recyclerView.setAdapter(mGridAdapter);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                        recyclerView.enableDefaultSwipeRefresh(true);
                        recyclerView.setHasFixedSize(false);
                        recyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                recyclerView.setRefreshing(true);
                                mGridAdapter.clearData();
                                mGridAdapter.notifyDataSetChanged();
                                mListings.getData().setAfter(null);
                                loadData(view);
                            }
                        });
                        recyclerView.reenableLoadmore();
                        recyclerView.setLoadMoreView(R.layout.bottom_progressbar);
                        recyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {

                            @Override
                            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                                loadMoreData(view);
                            }
                        });
                        recyclerView.setRefreshing(false);
                    } else {
                        loadFail(view);
                    }
                }

                @Override
                public void onFailure(Call<ListingsResponse> call, Throwable t) {
                    loadFail(view);
                }
            });
        } else {
            Snackbar snackbar = Snackbar.make(view, "Device is Offline", Snackbar.LENGTH_INDEFINITE).setAction("Try Again", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loadData(view);
                }
            });
            snackbar.show();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadMoreData(final View view) {
        if (isNetworkAvailable()) {
            RedditAPIClient.getListings(concatSubs(getSubs()), (mListings != null) ? mListings.getData().getAfter() : null).enqueue(new Callback<ListingsResponse>() {
                @Override
                public void onResponse(Call<ListingsResponse> call, final Response<ListingsResponse> response) {
                    if (response.code() == 200) {
                        mListings.getData().setAfter(response.body().getData().getAfter());
                        mGridAdapter.insert(filter(response.body().getData().getChildren()));
                        mGridAdapter.notifyDataSetChanged();
                    } else {
                        loadFail(view);
                    }
                }

                @Override
                public void onFailure(Call<ListingsResponse> call, Throwable t) {
                    loadFail(view);
                }
            });
        } else {
            Snackbar snackbar = Snackbar.make(view, "Device is Offline", Snackbar.LENGTH_INDEFINITE).setAction("Try Again", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loadData(view);
                }
            });
            snackbar.show();
        }
    }

    public static void update() {
        if (mGridAdapter != null) {
            //loadData();
            mGridAdapter.notifyDataSetChanged();
        }
    }

    private void loadFail(View view) {
        Snackbar snackbar = Snackbar.make(view, "An Error has Occurred", Snackbar.LENGTH_INDEFINITE).setAction("Try Again", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData(view);
            }
        });
        snackbar.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private List<ListingData> filter(List<ListingData> listing) {
        for (int i = 0; i < listing.size(); i++) {
            Listing _listing = listing.get(i).getData();
            if (isNSFW() && _listing.isNSFW() || _listing.getImageUrl() == null) {
                listing.remove(i);
            }
        }

        return listing;
    }

    private Boolean isNSFW() {
        return preferences.getBoolean("perform_nsfw", false);
    }

    private Set<String> getSubs() {
        Set<String> defaultVal = new HashSet<>(Arrays.asList("EarthPorn", "SpacePorn", "ExposurePorn", "Wallpapers"));
        return preferences.getStringSet("sync_sub", defaultVal);
    }

    private String concatSubs(Set<String> subs) {
        return TextUtils.join("+", subs.toArray());
    }
}