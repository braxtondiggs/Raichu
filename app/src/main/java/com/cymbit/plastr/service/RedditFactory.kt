package com.cymbit.plastr.service

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RedditFactory {
    private val redditClient = OkHttpClient().newBuilder().build()

    fun retrofit() : Retrofit = Retrofit.Builder()
        .client(redditClient)
        .baseUrl("https://api.reddit.com")
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val redditApi : RedditFetch.RedditApi = retrofit().create(RedditFetch.RedditApi::class.java)
}