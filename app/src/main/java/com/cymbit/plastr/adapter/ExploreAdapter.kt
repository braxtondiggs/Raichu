package com.cymbit.plastr.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.cymbit.plastr.AppDatabase
import com.cymbit.plastr.ImageActivity
import com.cymbit.plastr.R
import com.cymbit.plastr.service.FavoriteViewModel
import com.cymbit.plastr.service.RedditFetch
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.varunest.sparkbutton.SparkEventListener
import kotlinx.android.synthetic.main.grid_explore.view.*
import org.jetbrains.anko.doAsync

class ExploreAdapter(
    private var listing: MutableList<RedditFetch.RedditChildrenData>,
    private val favoriteViewModel: FavoriteViewModel
) :
    RecyclerView.Adapter<ExploreAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_explore, parent, false)
        return ViewHolder(view, parent.context, favoriteViewModel)
    }

    override fun getItemCount(): Int = listing.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun getItem(position: Int): RedditFetch.RedditChildrenData {
        return this.listing[position]
    }

    fun add(listing: List<RedditFetch.RedditChildrenData>) {
        this.listing.addAll(listing)
        notifyDataSetChanged()
    }

    fun clear() {
        this.listing.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val view: View,
        private val context: Context,
        private val favoriteViewModel: FavoriteViewModel
    ) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var listing: RedditFetch.RedditChildrenData
        private val db = Room.databaseBuilder(context, AppDatabase::class.java, "RedditChildrenData").build()

        init {
            view.setOnClickListener(this)
        }

        fun bind(listing: RedditFetch.RedditChildrenData) {
            this.listing = listing
            view.title.text = listing.title
            view.sub.text = "/r/".plus(listing.subreddit)
            view.favorite.isChecked = listing.is_favorite
            Picasso.get().load(listing.thumbnail).fit().centerCrop().into(view.image)


            view.favorite.setEventListener(object : SparkEventListener {
                override fun onEventAnimationStart(button: ImageView?, buttonState: Boolean) {}

                override fun onEventAnimationEnd(button: ImageView?, buttonState: Boolean) {}

                override fun onEvent(button: ImageView, buttonState: Boolean) {
                    doAsync {
                        if (buttonState) {
                            db.redditDao().insert(listing)
                            favoriteViewModel.insert(listing)
                            Snackbar.make(view, context.getString(R.string.favorite_add), Snackbar.LENGTH_SHORT).show()
                        } else {
                            db.redditDao().delete(listing)
                            favoriteViewModel.delete(listing)
                            Snackbar.make(view, context.getString(R.string.favorite_remove), Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            })
        }

        override fun onClick(v: View) {
            val intent = Intent(context, ImageActivity::class.java)
            intent.putExtra("LISTING_DATA", listing)
            context.startActivity(intent)
        }
    }
}