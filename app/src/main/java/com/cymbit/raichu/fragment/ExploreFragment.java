package com.cymbit.raichu.fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cymbit.raichu.R;
import com.cymbit.raichu.adapter.ExploreAdapter;
import com.cymbit.raichu.api.RedditAPIClient;
import com.cymbit.raichu.model.ListingsResponse;
import com.cymbit.raichu.utils.preferences.JSONSharedPreferences;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.grid.BasicGridLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ExploreFragment extends Fragment {
    @BindView(R.id.explore_recycler)
    UltimateRecyclerView recyclerView;
    @BindView(R.id.loading)
    SmoothProgressBar loading;
    @BindView(R.id.loadingCircle)
    ProgressBar loadingCircle;

    private Unbinder unbinder;
    private BasicGridLayoutManager mGridLayoutManager;
    protected ExploreAdapter mGridAdapter = null;
    private JSONArray mSubs = new JSONArray();

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
        loadData(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private JSONArray getSubs(Context c) {
        JSONArray subs = new JSONArray();
        try {
            subs = JSONSharedPreferences.loadJSONArray(c, "cymbit", "subs");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return subs;
    }

    private void loadData(final View view) {
        loading.progressiveStart();
        loadingCircle.setVisibility(View.VISIBLE);
        if (isNetworkAvailable()) {
            mSubs = getSubs(view.getContext());
            RedditAPIClient.getListings("Wallpapers", "hot").enqueue(new Callback<ListingsResponse>() {
                @Override
                public void onResponse(Call<ListingsResponse> call, final Response<ListingsResponse> response) {
                    loading.progressiveStop();
                    loadingCircle.setVisibility(View.GONE);
                    if (response.code() == 200) {
                        mGridAdapter = new ExploreAdapter(response.body().getData().getChildren());
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
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        recyclerView.setRefreshing(false);
                                        loadData(view);
                                    }
                                }, 1000);
                            }
                        });
                        recyclerView.reenableLoadmore();
                        recyclerView.setLoadMoreView(R.layout.bottom_progressbar);
                        recyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {

                            @Override
                            public void loadMore(int itemsCount, int maxLastVisiblePosition) {
                                System.out.println("loading more");
                            }
                        });
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
            Snackbar snackbar = Snackbar.make(view, "Device is Offline", Snackbar.LENGTH_SHORT).setAction("Try Again", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loadData(view);
                }
            });
            snackbar.show();
        }
    }

    private void loadFail(View view) {
        Snackbar snackbar = Snackbar.make(view, "An Error has Occurred", Snackbar.LENGTH_SHORT).setAction("Try Again", new View.OnClickListener() {
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
}