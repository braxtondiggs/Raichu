package com.cymbit.plastr.service

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FavoriteViewModel : ViewModel() {

    val favoritesLiveData = MutableLiveData<List<RedditFetch.RedditChildrenData>>()
    private var favorites = mutableListOf<RedditFetch.RedditChildrenData>()

    fun insert(favorite: RedditFetch.RedditChildrenData) {
        favorites.add(favorite)
        favoritesLiveData.postValue(favorites)
    }

    fun delete(favorite: RedditFetch.RedditChildrenData) {
        favorites.removeAt(favorites.indexOfFirst { it.id  == favorite.id })
        favoritesLiveData.postValue(favorites)
    }

    fun setData(_favorites: List<RedditFetch.RedditChildrenData>) {
        favorites.addAll(_favorites)
        favoritesLiveData.value = favorites
    }

    fun clearData() {
        favorites.clear()
        favoritesLiveData.postValue(favorites)
    }

}