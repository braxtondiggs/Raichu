package com.cymbit.plastr.service

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RedditFactory {
    private val redditClient = OkHttpClient().newBuilder().connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS).build()

    private fun retrofit(): Retrofit = Retrofit.Builder()
        .client(redditClient)
        .baseUrl("https://api.reddit.com")
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val redditApi: RedditFetch.RedditApi = retrofit().create(RedditFetch.RedditApi::class.java)
}