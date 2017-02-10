package com.cymbit.raichu.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cymbit.raichu.R;
import com.cymbit.raichu.adapter.FavoriteAdapter;
import com.cymbit.raichu.model.Favorites;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.grid.BasicGridLayoutManager;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;


public class FavoriteFragment extends Fragment {
    private Unbinder unbinder;
    public static List<Favorites> favorites;
    @SuppressLint("StaticFieldLeak")
    static FavoriteAdapter mGridAdapter = null;
    @SuppressLint("StaticFieldLeak")
    static UltimateRecyclerView recyclerView;

    public FavoriteFragment() {
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
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        unbinder = ButterKnife.bind(this, view);
        recyclerView = (UltimateRecyclerView) view.findViewById(R.id.favorite_recycler);
        favorites = Favorites.listAll(Favorites.class);

        mGridAdapter = new FavoriteAdapter(favorites);
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
                update();
                recyclerView.setRefreshing(false);
            }
        });
        recyclerView.setRefreshing(false);

        return view;
    }

    public static void update() {
        if (mGridAdapter != null) {
            favorites.clear();
            favorites.addAll(Favorites.listAll(Favorites.class));
            mGridAdapter.notifyDataSetChanged();
            if (favorites.isEmpty()) {
                recyclerView.showEmptyView();
            } else {
                recyclerView.hideEmptyView();
            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}