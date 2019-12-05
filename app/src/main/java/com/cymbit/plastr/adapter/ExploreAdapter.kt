package com.cymbit.plastr.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.cymbit.plastr.ImageActivity
import com.cymbit.plastr.MainActivity
import com.cymbit.plastr.R
import com.cymbit.plastr.helpers.Firebase
import com.cymbit.plastr.helpers.Preferences
import com.cymbit.plastr.helpers.Utils
import com.cymbit.plastr.service.RedditFetch
import com.google.firebase.firestore.FirebaseFirestore
import com.varunest.sparkbutton.SparkEventListener
import kotlinx.android.synthetic.main.grid_explore.view.*
import org.jetbrains.anko.backgroundColor
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

    class ViewHolder(private val view: View, private val context: Context, private val parent_view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private lateinit var listing: RedditFetch.RedditChildrenData
        private var background: Int = 0
        private val fb: Firebase = Firebase()
        private val db = FirebaseFirestore.getInstance()

        init {
            view.setOnClickListener(this)
        }

        fun bind(listing: RedditFetch.RedditChildrenData) {
            this.listing = listing
            view.title.text = Utils().convertEntity(listing.title)
            view.sub.text = "/r/".plus(listing.subreddit)
            db.document("favorites/" + fb.auth.currentUser?.uid + listing.id).addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                view.favorite.isChecked = snapshot != null && snapshot.exists()
            }
            Glide.with(context).load(getImage()).error(R.mipmap.ic_launcher_foreground).thumbnail(
                Glide.with(context).load(getImage(true)).apply(RequestOptions()).centerCrop()).transition(
                DrawableTransitionOptions.withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build())).listener(object :
                RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    Palette.Builder(resource.toBitmap()).generate {
                        it?.let { p ->
                            background = p.getDominantColor(ContextCompat.getColor(context,
                                R.color.initial_background))
                            view.bottom_container.backgroundColor = background
                        }
                    }
                    return false
                }

            }).error(R.mipmap.ic_launcher_foreground).centerCrop().into(view.image)

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
            intent.putExtra("COLOR", background)
            intent.putExtra("LISTING_DATA", listing)
            (context as Activity).startActivityForResult(intent, 1)
        }

        private fun getImage(thumbnail: Boolean = false): String {
            val quality = Preferences().getImageQuality(context)
            val images = listing.preview?.images
            val resolutions = if (!images.isNullOrEmpty()) images[0].resolutions else null
            val image = if (!resolutions.isNullOrEmpty()) resolutions[if (quality && !thumbnail) resolutions.lastIndex else 1] else null
            return if (!image?.url.isNullOrEmpty()) {
                fixUrl(image?.url.toString())
            } else if (thumbnail) {
                listing.thumbnail
            } else {
                listing.url
            }
        }

        private fun fixUrl(url: String): String {
            return url.replace("amp;", "")
        }
    }
}