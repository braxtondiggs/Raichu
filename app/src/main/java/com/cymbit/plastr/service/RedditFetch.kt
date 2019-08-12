package com.cymbit.plastr.service

import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


object RedditFetch {

    data class RedditChildrenData(
        val subreddit: String,
        val title: String,
        val hidden: Boolean,
        val downs: Int,
        val name: String,
        val ups: Int,
        val score: Int,
        val thumbnail: String,
        val is_self: Boolean,
        val created: Double,
        val domain: String,
        val preview: ImagePreview,
        val over_18: Boolean,
        val is_video: Boolean
    )

    data class ImagePreview (
        val enabled: Boolean
    )

    data class RedditResponse(
        val data: RedditData
    )

    data class RedditData(
        val children: List<RedditChildren>,
        val after: String,
        val before: String
    )

    data class RedditChildren (
        val data: RedditChildrenData
    )

    interface RedditApi {
        @GET("/r/{subreddit}/hot.json")
        fun getListings(@Path("subreddit") subreddit: String, @Query("after") after: String, @Query("limit") limit: Int): Deferred<Response<RedditFetch.RedditResponse>>
    }

}