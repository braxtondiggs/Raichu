package com.cymbit.plastr.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.cymbit.plastr.R
import com.cymbit.plastr.adapter.ExploreAdapter
import com.cymbit.plastr.service.FavoriteViewModel
import com.cymbit.plastr.service.RedditFetch
import kotlinx.android.synthetic.main.fragment_explore.*

class FavoriteFragment : Fragment() {
    private lateinit var mGridAdapter: ExploreAdapter
    private lateinit var favorites: List<RedditFetch.RedditChildrenData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val favoriteViewModel = ViewModelProviders.of(this).get(FavoriteViewModel::class.java)
        favoriteViewModel.favoritesLiveData.observe(viewLifecycleOwner, Observer<List<RedditFetch.RedditChildrenData>> { _favorites ->
            favorites = _favorites
            favorites.forEach { it.is_favorite = true }
            if (!this::mGridAdapter.isInitialized) {
                rvItems.layoutManager = GridLayoutManager(context, 2)
                mGridAdapter = ExploreAdapter(favorites, favoriteViewModel)
                rvItems.adapter = mGridAdapter
            } else {
                mGridAdapter.notifyDataSetChanged()
            }
        })
    }

}