package com.cymbit.plastr.service

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class RedditViewModel : ViewModel() {

    private val parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default

    private val scope = CoroutineScope(coroutineContext)

    private val repository: RedditRepository = RedditRepository(RedditFactory.redditApi)


    val redditLiveData = MutableLiveData<RedditFetch.RedditData>()

    fun fetchData(subreddit: String, after: String) {
        scope.launch {
            val redditData = repository.getListings(subreddit, after)
            redditLiveData.postValue(filter(redditData))
        }
    }


    fun cancelAllRequests() = coroutineContext.cancel()

    private fun filter(data: RedditFetch.RedditData?): RedditFetch.RedditData? {
        data!!.children = data.children.filterNot { (o) -> o.is_self || o.is_video }
        return data
    }

    fun clearData() {
        redditLiveData.postValue(null)
    }
}