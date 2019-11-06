package com.cymbit.plastr.service

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cymbit.plastr.helpers.Constants
import com.cymbit.plastr.helpers.Preferences
import com.cymbit.plastr.service.BaseRepository.Result
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


    val redditLiveData = MutableLiveData<Resource<RedditFetch.RedditData>>()

    fun fetchData(subreddit: String, sort: String, time: String?, after: String?, context: Context, search: Boolean = false) {
        scope.launch {
            val nsfw = if (Preferences().getNSFW(context)) "1" else "0"
            when (val result = repository.getListings(subreddit, sort, time, after, nsfw)) {
                is Result.Success -> {
                    result.data.data.search = search
                    result.data.data.sort = sort
                    result.data.data.time = time
                    result.data.data.subreddit = subreddit
                    redditLiveData.postValue(Resource.Success(filter(result.data.data, context)))
                }
                is Result.Error -> {
                    redditLiveData.postValue(Resource.Error(result.exception))
                }
            }
        }
    }


    @Suppress("unused")
    fun cancelAllRequests() = coroutineContext.cancel()

    private fun filter(data: RedditFetch.RedditData, context: Context): RedditFetch.RedditData {
        data.children = data.children.filterNot { (o) ->
            o.is_self || o.is_video || o.media !== null || (o.over_18 && Preferences().getNSFW(context)) || !Constants.VALID_DOMAINS.any { o.domain.contains(it) } || o.url.contains(".gif") || !this.hasImage(o)
        }
        return data
    }

    private fun hasImage(listing: RedditFetch.RedditChildrenData): Boolean {
        return listing.preview?.images!!.isNotEmpty() && !listing.preview?.images?.get(0)?.resolutions?.get(1)?.url.isNullOrEmpty() || listing.thumbnail.isNotEmpty()
    }

    fun clearData() {
        redditLiveData.postValue(null)
    }

    sealed class Resource<out T> {
        data class Success(val data: RedditFetch.RedditData) : Resource<RedditFetch.RedditData>()
        data class Error(val exception: String?) : Resource<Nothing>()
    }
}