package com.cymbit.plastr.service

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


object RedditFetch {

    @Parcelize
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
        val author: String,
        val id: String,
        val url: String,
        val is_video: Boolean,
        val permalink: String,
        val num_comments: Double
    ) : Parcelable

    @Parcelize
    data class ImagePreview (
        val enabled: Boolean,
        val images: List<Image>
    ): Parcelable

    @Parcelize
    data class Image (
        val source: Source,
        val id: String
    ): Parcelable

    @Parcelize
    data class Source (
        val url: String,
        val width: Double,
        val height: Double
    ): Parcelable

    @Parcelize
    data class RedditResponse(
        val data: RedditData
    ) : Parcelable

    @Parcelize
    data class RedditData(
        val children: List<RedditChildren>,
        val after: String,
        val before: String
    ) : Parcelable

    @Parcelize
    data class RedditChildren (
        val data: RedditChildrenData
    ) : Parcelable

    interface RedditApi {
        @GET("/r/{subreddit}/hot.json")
        fun getListings(@Path("subreddit") subreddit: String, @Query("after") after: String, @Query("limit") limit: Int): Deferred<Response<RedditFetch.RedditResponse>>
    }

}