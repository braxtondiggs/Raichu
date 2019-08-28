package com.cymbit.plastr.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.cymbit.plastr.AppDatabase
import com.cymbit.plastr.R
import com.cymbit.plastr.adapter.ExploreAdapter
import com.cymbit.plastr.helpers.InternetCheck
import com.cymbit.plastr.helpers.PaginationScrollListener
import com.cymbit.plastr.helpers.Preferences
import com.cymbit.plastr.service.FavoriteViewModel
import com.cymbit.plastr.service.RedditFetch
import com.cymbit.plastr.service.RedditViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_explore.*

class ExploreFragment : Fragment() {
    private lateinit var redditViewModel: RedditViewModel
    private lateinit var favoriteViewModel: FavoriteViewModel
    private var after: String = ""
    private var isLastPage = false
    private var isLoading = false
    private lateinit var mGridAdapter: ExploreAdapter
    private val listings: ArrayList<RedditFetch.RedditChildren> = ArrayList()
    private lateinit var db: AppDatabase
    private lateinit var favorites: List<RedditFetch.RedditChildrenData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvItems.layoutManager = GridLayoutManager(context, 2)
        db = Room.databaseBuilder(context!!, AppDatabase::class.java, "RedditChildrenData").build()

        InternetCheck(object : InternetCheck.Consumer {
            override fun accept(internet: Boolean?) {
                if (internet!!) {
                    loadData()
                } else {
                    deviceOffline(view).setAction(R.string.try_again) { loadData() }.show()
                }
            }
        })
    }

    private fun deviceOffline(view: View): Snackbar {
        return Snackbar.make(view, R.string.offline, Snackbar.LENGTH_INDEFINITE)
    }

    private fun loadData() {
        activity?.let {
            redditViewModel = ViewModelProviders.of(it).get(RedditViewModel::class.java)
            redditViewModel.fetchData(Preferences().getSubs(context!!).joinToString("+"), after)
            redditViewModel.redditLiveData.observe(this, Observer { value ->
                if (value !== null) {
                    isLoading = true
                    isLastPage = false
                    after = ""
                    listings.clear()
                    listings.addAll(value.children)
                    swipeLayout.isRefreshing = false
                    if (after.isBlank() && !this::mGridAdapter.isInitialized) {
                        initGridView()
                    } else {
                        isLoading = false
                        loading_circle.visibility = View.GONE
                        loading.visibility = View.GONE
                        checkFavorites()
                        mGridAdapter.clear()
                        mGridAdapter.add(listings.map { (v) -> v })
                    }
                    after = value.after
                }
            })
        }
    }

    private fun loadMoreData(view: View) {
        InternetCheck(object : InternetCheck.Consumer {
            override fun accept(internet: Boolean?) {
                if (internet!!) {
                    redditViewModel.fetchData(
                        Preferences().getSubs(context!!).joinToString("+"),
                        after
                    )
                } else {
                    deviceOffline(view).setAction(R.string.try_again) { loadMoreData(view) }.show()
                }
            }
        })
    }

    private fun initGridView() {
        loading_circle.visibility = View.GONE
        favoriteViewModel = ViewModelProviders.of(this).get(FavoriteViewModel::class.java)
        mGridAdapter = ExploreAdapter(listings.map { (v) -> v }.toMutableList(), favoriteViewModel)
        rvItems.adapter = mGridAdapter
        swipeLayout.setOnRefreshListener {
            redditViewModel.clearData()
            redditViewModel.fetchData(Preferences().getSubs(context!!).joinToString("+"), after)
        }

        rvItems.addOnScrollListener(object :
            PaginationScrollListener(rvItems.layoutManager as LinearLayoutManager) {
            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

            override fun loadMoreItems() {
                isLoading = true
                loading.visibility = View.VISIBLE
                loadMoreData(view!!)
            }
        })

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
                empty_view.visibility = (if (mGridAdapter.itemCount == 0 && !isLoading) View.VISIBLE else View.GONE)
            }
        })

        db.redditDao().getAll().observe(
            this,
            Observer<List<RedditFetch.RedditChildrenData>> { _favorites ->
                favoriteViewModel.setData(_favorites)
            })

        favoriteViewModel.favoritesLiveData.observe(
            viewLifecycleOwner,
            Observer<List<RedditFetch.RedditChildrenData>> { _favorites ->
                favorites = _favorites
                checkFavorites()
                mGridAdapter.notifyDataSetChanged()
            })
    }

    private fun checkFavorites() {
        listings.map { (listing) ->
            listing.is_favorite = favorites.indexOfFirst { fav -> fav.id == listing.id } > 0
        }
    }
}