package com.cymbit.raichu.api;

import com.cymbit.raichu.model.ListingsResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

class RedditAPI {
    private static final String BASE_URL = "https://api.reddit.com";

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

    private static final Retrofit retrofit = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL).build();

    static final Subreddit subreddit = retrofit.create(Subreddit.class);

    interface Subreddit {

        @GET("/r/{subreddit}/hot.json")
        Call<ListingsResponse> getListings(@Path("subreddit") String subreddit, @Query("after") String after, @Query("limit") int limit);
    }
}
