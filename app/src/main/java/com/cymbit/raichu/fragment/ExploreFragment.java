package com.cymbit.raichu.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cymbit.raichu.ImageViewActivity;
import com.cymbit.raichu.R;
import com.cymbit.raichu.adapter.GridViewAdapter;
import com.cymbit.raichu.api.RedditAPIClient;
import com.cymbit.raichu.model.ListingsResponse;
import com.cymbit.raichu.utils.preferences.JSONSharedPreferences;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.marshalchen.ultimaterecyclerview.ItemTouchListenerAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.grid.BasicGridLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ExploreFragment extends Fragment {
    @BindView(R.id.explore_recycler)
    UltimateRecyclerView recyclerView;

    private Unbinder unbinder;
    private BasicGridLayoutManager mGridLayoutManager;
    protected GridViewAdapter mGridAdapter = null;
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
        mSubs = getSubs(view.getContext());
        RedditAPIClient.getListings("Wallpapers", "hot").enqueue(new Callback<ListingsResponse>() {
            @Override
            public void onResponse(Call<ListingsResponse> call, final Response<ListingsResponse> response) {
                mGridAdapter = new GridViewAdapter(response.body().getData().getChildren());
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
                ItemTouchListenerAdapter itemTouchListenerAdapter = new ItemTouchListenerAdapter(recyclerView.mRecyclerView, new ItemTouchListenerAdapter.RecyclerViewOnItemClickListener() {
                    @Override
                    public void onItemClick(RecyclerView parent, View clickedView, int position) {
                        Intent myIntent = new Intent(view.getContext(), ImageViewActivity.class);
                        //myIntent.putExtra("listing", Parcels.wrap(listing));
                        view.getContext().startActivity(myIntent);
                    }

                    @Override
                    public void onItemLongClick(RecyclerView parent, View clickedView, int position) {

                    }


                });
                recyclerView.mRecyclerView.addOnItemTouchListener(itemTouchListenerAdapter);
            }

            @Override
            public void onFailure(Call<ListingsResponse> call, Throwable t) {
                // Log error here since request failed
            }
        });
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
}