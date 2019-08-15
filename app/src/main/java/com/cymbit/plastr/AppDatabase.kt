package com.cymbit.plastr

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cymbit.plastr.service.RedditDAO
import com.cymbit.plastr.service.RedditFetch

@Database (entities = [(RedditFetch.RedditChildrenData::class)],version = 1,exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun redditDao(): RedditDAO
}