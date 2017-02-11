package com.cymbit.raichu.fragment;

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

import com.afollestad.materialdialogs.MaterialDialog;
import com.cymbit.raichu.MainActivity;
import com.cymbit.raichu.R;
import com.cymbit.raichu.adapter.ExploreAdapter;
import com.cymbit.raichu.api.RedditAPIClient;
import com.cymbit.raichu.model.Listing;
import com.cymbit.raichu.model.ListingsData;
import com.cymbit.raichu.model.ListingsResponse;
import com.cymbit.raichu.utils.Preferences;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.grid.BasicGridLayoutManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

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
    public ExploreAdapter mGridAdapter = null;
    SharedPreferences preferences;
    public static Bus bus;
    public String mSearch = null;
    public View view;
    Context mContext;

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
        view = inflater.inflate(R.layout.fragment_explore, container, false);
        unbinder = ButterKnife.bind(this, view);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mContext = getContext();

        bus = MainActivity.bus;
        bus.register(this);
        loadData();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void loadData() {
        loading.progressiveStart();
        loadingCircle.setVisibility(View.VISIBLE);
        if (isNetworkAvailable()) {
            RedditAPIClient.getListings(((mSearch == null) ? concatSubs(Preferences.getSubs(getActivity())) : mSearch), (mListings != null) ? mListings.getData().getAfter() : null).enqueue(new Callback<ListingsResponse>() {
                @Override
                public void onResponse(Call<ListingsResponse> call, final Response<ListingsResponse> response) {
                    loading.progressiveStop();
                    loadingCircle.setVisibility(View.GONE);
                    if (response.code() == 200) {
                        if (!response.body().getData().getChildren().isEmpty()) {
                            mListings = response.body();
                            mGridAdapter = new ExploreAdapter(filter(mListings.getData().getChildren()));
                            mGridAdapter.setSpanColumns(2);
                            BasicGridLayoutManager mGridLayoutManager = new BasicGridLayoutManager(view.getContext(), 2, mGridAdapter);
                            recyclerView.setLayoutManager(mGridLayoutManager);
                            recyclerView.setAdapter(mGridAdapter);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                            recyclerView.enableDefaultSwipeRefresh(true);
                            recyclerView.setHasFixedSize(false);
                            recyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                @Override
                                public void onRefresh() {
                                    recyclerView.setRefreshing(true);
                                    mGridAdapter.clear();
                                    mListings.getData().setAfter(null);
                                    loadData();
                                }
                            });
                            recyclerView.reenableLoadmore();
                            recyclerView.setLoadMoreView(R.layout.bottom_progressbar);
                            recyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {

                                @Override
                                public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                                    loadMoreData();
                                }
                            });
                            recyclerView.setRefreshing(false);
                            recyclerView.hideEmptyView();
                        } else {
                            emptyList().show();
                            recyclerView.showEmptyView();
                        }
                    } else {
                        loadFail().show();
                    }
                }

                @Override
                public void onFailure(Call<ListingsResponse> call, Throwable t) {
                    loadFail().show();
                }
            });
        } else {
            deviceOffline().show();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadMoreData() {
        if (isNetworkAvailable()) {
            RedditAPIClient.getListings(((mSearch == null) ? concatSubs(Preferences.getSubs(getActivity())) : mSearch), (mListings != null) ? mListings.getData().getAfter() : null).enqueue(new Callback<ListingsResponse>() {
                @Override
                public void onResponse(Call<ListingsResponse> call, final Response<ListingsResponse> response) {
                    if (response.code() == 200) {
                        if (!response.body().getData().getChildren().isEmpty()) {
                            mListings.getData().setAfter(response.body().getData().getAfter());
                            mGridAdapter.insert(filter(response.body().getData().getChildren()));
                            loading.progressiveStop();
                            recyclerView.hideEmptyView();
                        } else {
                            emptyList().show();
                            recyclerView.showEmptyView();
                        }
                    } else {
                        loadFail().show();
                    }
                }

                @Override
                public void onFailure(Call<ListingsResponse> call, Throwable t) {
                    loadFail().show();
                }
            });
        } else {
            deviceOffline().show();
        }
    }

    private MaterialDialog.Builder emptyList() {
        return new MaterialDialog.Builder(mContext)
                .title(mContext.getResources().getString(R.string.error))
                .content(mContext.getResources().getString(R.string.empty_explore1) + mContext.getResources().getString(R.string.empty_explore2))
                .positiveText(mContext.getResources().getString(R.string.ok));
    }

    private Snackbar loadFail() {
        return Snackbar.make(view, mContext.getResources().getString(R.string.error), Snackbar.LENGTH_INDEFINITE).setAction(mContext.getResources().getString(R.string.try_again), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
    }

    private Snackbar deviceOffline() {
        return Snackbar.make(view, mContext.getResources().getString(R.string.offline), Snackbar.LENGTH_INDEFINITE).setAction(mContext.getResources().getString(R.string.try_again), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private List<ListingsData.ListingData> filter(List<ListingsData.ListingData> listing) {
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

    private String concatSubs(Set<String> subs) {
        return TextUtils.join("+", subs.toArray());
    }

    @Subscribe
    public void getMessage(MainActivity.OttoData data) {
        if (data.action.equals("search")) {
            mSearch = data.query;
            mListings = null;
            recyclerView.setLoadMoreView(null);
            mGridAdapter.clear();
            loadData();
        }
    }
}