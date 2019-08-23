package com.cymbit.plastr.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cymbit.plastr.R
import com.cymbit.plastr.adapter.ExploreAdapter
import com.cymbit.plastr.service.FavoriteViewModel
import com.cymbit.plastr.service.RedditFetch
import kotlinx.android.synthetic.main.fragment_explore.*

class FavoriteFragment : Fragment() {
    private lateinit var mGridAdapter: ExploreAdapter
    private lateinit var favorites: List<RedditFetch.RedditChildrenData>
    private lateinit var favoriteViewModel: FavoriteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favoriteViewModel = ViewModelProviders.of(this).get(FavoriteViewModel::class.java)
        favoriteViewModel.favoritesLiveData.observe(viewLifecycleOwner, Observer<List<RedditFetch.RedditChildrenData>> { _favorites ->
            favorites = _favorites
            favorites.forEach { it.is_favorite = true }
            if (!this::mGridAdapter.isInitialized) {
                initGridView()
            } else {
                mGridAdapter.notifyDataSetChanged()
            }
        })
    }
    private fun initGridView() {
        rvItems.layoutManager = GridLayoutManager(context, 2)
        mGridAdapter = ExploreAdapter(favorites, favoriteViewModel)
        rvItems.adapter = mGridAdapter
        mGridAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkEmpty()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkEmpty()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkEmpty()
            }

            fun checkEmpty() {
                empty_view.visibility = (if (mGridAdapter.itemCount == 0) View.VISIBLE else View.GONE)
            }
        })
    }
}