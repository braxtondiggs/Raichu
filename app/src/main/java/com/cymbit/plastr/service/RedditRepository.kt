package com.cymbit.plastr.service

class RedditRepository(private val api : RedditFetch.RedditApi) : BaseRepository() {

    suspend fun getListings(subreddit: String, sort: String, time: String, after: String, include_over_18: String) : Result<RedditFetch.RedditResponse> {
        return safeApiResult(
        call = { api.getListings(subreddit, sort, time, after, 25, "0", include_over_18).await()}
    )}
}