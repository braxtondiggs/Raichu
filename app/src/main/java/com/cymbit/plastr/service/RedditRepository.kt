package com.cymbit.plastr.service

class RedditRepository(private val api : RedditFetch.RedditApi) : BaseRepository() {

    suspend fun getListings(subreddit: String, after: String) : RedditFetch.RedditData?{
        val redditResponse = safeApiCall(
            call = {api.getListings(subreddit, after, 25).await()},
            errorMessage = "Error Fetching Reddit"
        )
        return redditResponse?.data

    }

}