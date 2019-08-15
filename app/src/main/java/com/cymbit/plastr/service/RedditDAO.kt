package com.cymbit.plastr.service

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RedditDAO {
    @Query("Select * from Favorites")
    fun getAll() : LiveData<List<RedditFetch.RedditChildrenData>>

    @Delete
    fun delete(listing: RedditFetch.RedditChildrenData)

    @Query("SELECT * FROM Favorites WHERE id IN (:id)")
    fun findById(id: String): LiveData<RedditFetch.RedditChildrenData>

    @Insert
    fun insert(listing: RedditFetch.RedditChildrenData)

}