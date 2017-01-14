package com.cymbit.raichu.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.cymbit.raichu.R;
import com.cymbit.raichu.adapter.GridViewAdapter;
import com.cymbit.raichu.api.RedditAPIClient;
import com.cymbit.raichu.model.ListingsResponse;
import com.cymbit.raichu.utils.preferences.JSONSharedPreferences;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;

import org.json.JSONArray;
import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ExploreFragment extends Fragment {
    @BindView(R.id.explore_grid)
    GridView gridView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeLayout;

    private Unbinder unbinder;
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
            public void onResponse(Call<ListingsResponse> call, Response<ListingsResponse> response) {
                gridView.setAdapter(new GridViewAdapter(view.getContext(), response.body().getData().getChildren()));
            }

            @Override
            public void onFailure(Call<ListingsResponse> call, Throwable t) {
                // Log error here since request failed
            }
        });
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSubs = getSubs(view.getContext());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                }, 5000);
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