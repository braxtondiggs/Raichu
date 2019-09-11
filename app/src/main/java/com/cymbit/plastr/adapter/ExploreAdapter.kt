package com.cymbit.plastr.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cymbit.plastr.ImageActivity
import com.cymbit.plastr.MainActivity
import com.cymbit.plastr.R
import com.cymbit.plastr.helpers.Firebase
import com.cymbit.plastr.service.RedditFetch
import com.google.firebase.firestore.FirebaseFirestore
import com.varunest.sparkbutton.SparkEventListener
import kotlinx.android.synthetic.main.grid_explore.view.*
import org.jetbrains.anko.doAsync

class ExploreAdapter(private var listing: MutableList<RedditFetch.RedditChildrenData>, private var parent_view: View) :
    RecyclerView.Adapter<ExploreAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_explore, parent, false)
        return ViewHolder(view, parent.context, parent_view)
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

    class ViewHolder(private val view: View, private val context: Context, private val parent_view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var listing: RedditFetch.RedditChildrenData
        private val fb: Firebase = Firebase()
        private val db = FirebaseFirestore.getInstance()
        init {
            view.setOnClickListener(this)
        }

        fun bind(listing: RedditFetch.RedditChildrenData) {
            this.listing = listing
            view.title.text = listing.title
            view.sub.text = "/r/".plus(listing.subreddit)
            db.document("favorites/" + fb.auth.currentUser?.uid + listing.id).addSnapshotListener { snapshot, e  ->
                if (e != null) return@addSnapshotListener
                view.favorite.isChecked = snapshot != null && snapshot.exists()
            }
            Glide.with(context).load(listing.thumbnail).error(R.mipmap.ic_launcher_foreground).centerCrop().into(view.image)


            view.favorite.setEventListener(object : SparkEventListener {
                override fun onEventAnimationStart(button: ImageView?, buttonState: Boolean) {}

                override fun onEventAnimationEnd(button: ImageView?, buttonState: Boolean) {}

                override fun onEvent(button: ImageView, buttonState: Boolean) {
                    doAsync {
                        if (buttonState) {
                            fb.favorite(listing, context as MainActivity, parent_view, context.getString(R.string.favorite_add))
                        } else {
                            fb.unfavorite(listing, context as MainActivity, parent_view, context.getString(R.string.favorite_remove))
                        }
                    }
                }
            })
        }

        override fun onClick(v: View) {
            val intent = Intent(context, ImageActivity::class.java)
            intent.putExtra("LISTING_DATA", listing)
            (context as Activity).startActivityForResult(intent, 1)
        }
    }
}