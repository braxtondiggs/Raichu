package com.cymbit.plastr.fragment

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.cymbit.plastr.R
import com.cymbit.plastr.adapter.ExploreAdapter
import com.cymbit.plastr.helpers.InternetCheck
import com.cymbit.plastr.helpers.PaginationScrollListener
import com.cymbit.plastr.helpers.Preferences
import com.cymbit.plastr.service.RedditFetch
import com.cymbit.plastr.service.RedditViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_explore.*

class ExploreFragment : Fragment() {
    private lateinit var redditViewModel: RedditViewModel
    private var after: String? = null
    private var isLastPage = false
    private var isLoading = false
    private var isSearch = false
    private var tryAgainCount: Int = 0
    private lateinit var query: String
    private var menuSort: String = "hot"
    private var menuTime: String? = null
    private lateinit var mGridAdapter: ExploreAdapter
    private val listings: ArrayList<RedditFetch.RedditChildren> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvItems.layoutManager = GridLayoutManager(context, 2)
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
            redditViewModel.fetchData(
                Preferences().getSelectedSubs(context!!).joinToString("+"),
                menuSort,
                menuTime,
                after,
                context!!
            )
            redditViewModel.redditLiveData.observe(this, Observer { result ->
                when(@Suppress("UnnecessaryVariable") val value = result) {
                    is RedditViewModel.Resource.Success -> {
                        isSearch = value.data.search
                        menuSort = value.data.sort
                        menuTime = value.data.time
                        if (value.data.children.isNotEmpty() && value.data.after != null) {
                            query = value.data.children[0].data.subreddit
                            if (after.isNullOrBlank() && !this::mGridAdapter.isInitialized) {
                                listings.addAll(value.data.children)
                                initGridView()
                            } else if (isLoading) {
                                listings.addAll(value.data.children)
                                mGridAdapter.add(value.data.children.map { (v) -> v })
                                isLoading = false
                                isLastPage = false
                                loading.visibility = View.GONE
                            } else {
                                swipeLayout.isRefreshing = false
                                loading_circle.visibility = View.GONE
                                loading.visibility = View.GONE
                                listings.clear()
                                mGridAdapter.clear()
                                listings.addAll(value.data.children)
                                mGridAdapter.add(listings.map { (v) -> v })
                                isLoading = false
                                isLastPage = false
                            }
                            after = value.data.after
                            tryAgainCount = 0
                        } else {
                            isLoading = false
                            query = value.data.subreddit
                            if (value.data.after != null) after = value.data.after
                            if (!this::mGridAdapter.isInitialized) {
                                initGridView()
                                mGridAdapter.clear()
                            }
                            if (tryAgainCount < 2) {
                                tryAgainCount++
                                isLoading = true
                                loading.visibility = View.VISIBLE
                                Handler().postDelayed({
                                    view?.let { it -> loadMoreData(it) }
                                }, 1000)
                            } else {
                                view?.let { it -> Snackbar.make(it, R.string.no_images, Snackbar.LENGTH_INDEFINITE).setAction(R.string.try_again) { _ ->
                                    tryAgainCount = 0
                                    isLoading = true
                                    loading.visibility = View.VISIBLE
                                    loadMoreData(it)
                                }.show() }
                            }
                        }
                    }
                    is RedditViewModel.Resource.Error -> {
                        MaterialDialog(context!!).show {
                            title(R.string.error)
                            message(R.string.reddit_error)
                            positiveButton(R.string.ok)
                        }
                    }
                }
            })
        }
    }

    private fun loadMoreData(view: View) {
        InternetCheck(object : InternetCheck.Consumer {
            override fun accept(internet: Boolean?) {
                if (internet!!) {
                    if (!isSearch) query = Preferences().getSelectedSubs(context!!).joinToString("+")
                    redditViewModel.fetchData(query, menuSort, menuTime, after, context!!, isSearch)
                } else {
                    deviceOffline(view).setAction(R.string.try_again) { loadMoreData(view) }.show()
                }
            }
        })
    }

    private fun initGridView() {
        loading_circle.visibility = View.GONE
        mGridAdapter = view?.let { ExploreAdapter(listings.map { (v) -> v }.toMutableList(), it) }!!
        rvItems.adapter = mGridAdapter
        swipeLayout.setOnRefreshListener {
            after = ""
            redditViewModel.clearData()
            redditViewModel.fetchData(
                Preferences().getSelectedSubs(context!!).joinToString("+"),
                menuSort, menuTime,
                after,
                context!!
            )
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
                empty_view.visibility =
                    (if (mGridAdapter.itemCount == 0 && !isLoading) View.VISIBLE else View.GONE)
            }
        })
    }
}