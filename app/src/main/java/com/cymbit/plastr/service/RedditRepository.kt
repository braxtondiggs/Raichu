package com.cymbit.plastr.service

class RedditRepository(private val api : RedditFetch.RedditApi) : BaseRepository() {

    suspend fun getListings(subreddit: String, after: String) : Result<RedditFetch.RedditResponse> {
        return safeApiResult(
        call = { api.getListings(subreddit, after, 25).await()},
        errorMessage = "Error Fetching Reddit"
    )
    }

}