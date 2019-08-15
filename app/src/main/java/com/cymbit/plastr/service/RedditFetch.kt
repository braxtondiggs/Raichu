package com.cymbit.plastr.service

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


object RedditFetch {

    @Parcelize
    @Entity(tableName = "Favorites")
    data class RedditChildrenData(
        @ColumnInfo(name = "subreddit") var subreddit: String,
        @ColumnInfo(name = "title") var title: String,
        @Ignore var hidden: Boolean,
        @Ignore var downs: Int,
        @Ignore var name: String,
        @Ignore var ups: Int,
        @Ignore var score: Int,
        @ColumnInfo(name = "thumbnail") var thumbnail: String,
        @Ignore var is_self: Boolean,
        @ColumnInfo(name = "created") var created: Long,
        @Ignore var domain: String,
        // @Ignore var preview: ImagePreview,
        @Ignore var over_18: Boolean,
        @Ignore var author: String,
        @PrimaryKey var id: String,
        @Ignore var url: String,
        @Ignore var is_video: Boolean,
        @Ignore var permalink: String,
        @Ignore var num_comments: Double,
        @Ignore var is_favorite: Boolean
    ) : Parcelable {
        constructor() : this("", "", false, 0, "", 0, 0, "", false, 0, "", false, "", "", "", false, "", 0.0, false)
    }

    @Parcelize
    data class ImagePreview(
        val enabled: Boolean,
        val images: List<Image>
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
        val before: String
    ) : Parcelable

    @Parcelize
    data class RedditChildren(
        val data: RedditChildrenData
    ) : Parcelable

    interface RedditApi {
        @GET("/r/{subreddit}/hot.json")
        fun getListings(@Path("subreddit") subreddit: String, @Query("after") after: String, @Query("limit") limit: Int): Deferred<Response<RedditFetch.RedditResponse>>
    }

}