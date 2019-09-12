package com.cymbit.plastr.service

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    fun fetchData(subreddit: String, after: String, search: Boolean = false) {
        scope.launch {
            when (val result = repository.getListings(subreddit, after)) {
                is Result.Success -> {
                    result.data.data.search = search
                    redditLiveData.postValue(Resource.success(filter(result.data.data)))
                }
                is Result.Error -> {
                    redditLiveData.postValue(Resource.error(result.exception))
                }
            }
        }
    }


    fun cancelAllRequests() = coroutineContext.cancel()

    private fun filter(data: RedditFetch.RedditData): RedditFetch.RedditData {
        data.children = data.children.filterNot { (o) -> o.is_self || o.is_video }
        return data
    }

    fun clearData() {
        redditLiveData.postValue(null)
    }
}

data class Resource<out T>(val data: T?, val error: Exception?) {
    companion object {
        fun <T> success(data: T): Resource<T> {
            return Resource(data, null)
        }

        fun <T> error(error: Exception): Resource<T> {
            return Resource(null, error)
        }
    }
}