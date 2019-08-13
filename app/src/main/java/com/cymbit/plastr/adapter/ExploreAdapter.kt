package com.cymbit.plastr.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cymbit.plastr.ImageActivity
import com.cymbit.plastr.R
import com.cymbit.plastr.service.RedditFetch
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.grid_explore.view.*

class ExploreAdapter(private val listing: List<RedditFetch.RedditChildren>): RecyclerView.Adapter<ExploreAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_explore, parent,false)
        return ViewHolder(view, parent.context)
    }

    override fun getItemCount(): Int = listing.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun getItem(position: Int): RedditFetch.RedditChildrenData {
        return listing[position].data
    }

    class ViewHolder(private val view: View, private val context: Context) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var listing: RedditFetch.RedditChildrenData

        init {
            view.setOnClickListener(this)
        }

        fun bind(listing: RedditFetch.RedditChildrenData) {
            this.listing = listing
            view.title.text = listing.title
            view.sub.text = "/r/".plus(listing.subreddit)
            Picasso.get().load(listing.thumbnail).fit().centerCrop().into(view.image)
        }

        override fun onClick(v: View) {
            val intent = Intent(context, ImageActivity::class.java)
            intent.putExtra("LISTING_DATA", listing)
            context.startActivity(intent)
        }
    }
}