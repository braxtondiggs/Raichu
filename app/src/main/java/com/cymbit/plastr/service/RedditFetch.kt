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
        var subreddit: String,
        var title: String,
        var hidden: Boolean,
        var downs: Int,
        var name: String,
        var ups: Int,
        var score: Int,
        var thumbnail: String,
        var is_self: Boolean,
        var created: Long,
        var domain: String,
        var preview: ImagePreview?,
        var over_18: Boolean,
        var author: String,
        var id: String,
        var url: String,
        var is_video: Boolean,
        var media: Media?,
        var permalink: String,
        var num_comments: Double,
        var is_favorite: Boolean,
        var user: String
    ) : Parcelable {
        constructor() : this("", "", false, 0, "", 0, 0, "", false, 0, "", null,false, "", "", "", false, null,"", 0.0, false, "")
    }

    @Parcelize
    data class ImagePreview(
        val enabled: Boolean,
        val images: List<Image>
        //val reddit_video_preview: VideoPreview
    ) : Parcelable

    @Parcelize
    data class Image(
        val source: Source,
        val id: String
    ) : Parcelable

    @Parcelize
    data class Source(
        val url: String,
        val width: Double,
        val height: Double
    ) : Parcelable

    @Parcelize
    data class RedditResponse(
        val data: RedditData
    ) : Parcelable

    @Parcelize
    data class RedditData(
        var children: List<RedditChildren>,
        val after: String,
        val before: String,
        var search: Boolean
    ) : Parcelable

    @Parcelize
    data class Media(
        val type: String
    ): Parcelable

    @Parcelize
    data class VideoPreview(
        val fallback_url: String,
        val height: Int,
        val width: Int,
        val scrubber_media_url: String,
        val dash_url: String,
        val duration: Int,
        val hls_url: String,
        val is_gif: Boolean,
        val transcoding_status: String
    ) : Parcelable

    @Parcelize
    data class RedditChildren(
        val data: RedditChildrenData
    ) : Parcelable

    interface RedditApi {
        @GET("/r/{subreddit}/hot.json")
        fun getListings(@Path("subreddit") subreddit: String, @Query("after") after: String, @Query("limit") limit: Int, @Query("self") self: String, @Query("include_over_18") include_over_18: String): Deferred<Response<RedditResponse>>
    }

}