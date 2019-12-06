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
    data class RedditResponse(
        val data: RedditData
    ) : Parcelable

    @Parcelize
    data class RedditData(
        var children: List<RedditChildren>,
        val after: String?,
        val before: String?,
        var search: Boolean,
        var sort: String?,
        var time: String?,
        var subreddit: String
    ) : Parcelable

    @Parcelize
    data class RedditChildren(
        val data: RedditChildrenData
    ) : Parcelable

    @Parcelize
    data class RedditChildrenData(
        var subreddit: String = "",
        var title: String = "",
        var downs: Int = 0,
        var ups: Int = 0,
        var thumbnail: String = "",
        var is_self: Boolean = false,
        var created: Long = 0,
        var domain: String = "",
        var preview: ImagePreview? = ImagePreview(),
        var over_18: Boolean = false,
        var author: String = "",
        var id: String = "",
        var url: String = "",
        var is_video: Boolean = false,
        var media: Media? = null,
        var permalink: String = "",
        var user: String? = ""
    ) : Parcelable

    @Parcelize
    data class ImagePreview(
        val enabled: Boolean = false,
        val images: List<Image>? = listOf(),
        val reddit_video_preview: VideoPreview? = null
    ) : Parcelable

    @Parcelize
    data class Image(
        val source: Source = Source(),
        val id: String = "",
        val resolutions: List<Resolutions> = listOf()
    ) : Parcelable

    @Parcelize
    data class Source(
        val url: String = "",
        val width: Int = 0,
        val height: Int = 0
    ) : Parcelable

    @Parcelize
    data class Resolutions(
        val url: String = "",
        val width: Int = 0,
        val height: Int = 0
    ) : Parcelable

    @Parcelize
    data class Media(
        val type: String
    ) : Parcelable

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

    interface RedditApi {
        @GET("/r/{subreddit}/{sort}.json")
        fun getListingsAsync(@Path("subreddit") subreddit: String, @Path("sort") sort: String?, @Query("t") time: String?, @Query("after") after: String?, @Query("limit") limit: Int, @Query("self") self: String, @Query("include_over_18") include_over_18: String): Deferred<Response<RedditResponse>>
    }

}