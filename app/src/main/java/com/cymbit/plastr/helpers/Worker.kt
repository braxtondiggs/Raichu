package com.cymbit.plastr.helpers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cymbit.plastr.R
import com.cymbit.plastr.service.BaseRepository
import com.cymbit.plastr.service.RedditFactory
import com.cymbit.plastr.service.RedditFetch
import com.cymbit.plastr.service.RedditRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class Worker(private val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    private val repository: RedditRepository = RedditRepository(RedditFactory.redditApi)
    private var after: String? = null
    private var menuSort: String = "hot"
    private var menuTime: String? = null

    override suspend fun doWork(): Result = coroutineScope {
        val networkPref = Preferences().getNetworkPref(appContext)
        val notification = Preferences().getNotification(appContext)
        val network = checkNetworkConnection()
        if (network != null) {
            if (network == networkPref) {
                val nsfw = if (Preferences().getNSFW(appContext)) "1" else "0"
                val d = async {
                    when (val result = repository.getListings(Preferences().getSelectedSubs(appContext).joinToString("+"), menuSort, menuTime, after, nsfw)) {
                        is BaseRepository.Result.Success -> {
                            val data = result.data.data
                            val item = data.children[0]
                            val image = getImage(item.data)
                            after = data.after
                            Glide.with(appContext).asBitmap().load(image).into(object : CustomTarget<Bitmap>() {
                                override fun onLoadCleared(placeholder: Drawable?) {}

                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    setWallpaper(resource)
                                    if (notification) createNotification(item.data.title)
                                    Result.success()
                                }

                                override fun onLoadFailed(errorDrawable: Drawable?) {
                                    super.onLoadFailed(errorDrawable)
                                    Result.retry()
                                }

                            })

                        }
                        is BaseRepository.Result.Error -> {
                            return@async Result.retry()
                        }
                    }
                }
                d.await()
            }
        } else {
            Result.failure()
        }
        Result.failure()
    }

    @Suppress("DEPRECATION")
    private fun checkNetworkConnection(): Int? {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT < 23) {
            val ni = cm.activeNetworkInfo
            if (ni != null && ni.isConnected) {
                return if (ni.type == ConnectivityManager.TYPE_WIFI) {
                    0
                } else {
                    1
                }
            }
        } else {
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                if (nc != null) {
                    return if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        0
                    } else {
                        1
                    }
                }
            }
        }
        return null
    }

    private fun setWallpaper(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            wallpaper(WallpaperManager.FLAG_SYSTEM, bitmap)
            wallpaper(WallpaperManager.FLAG_LOCK, bitmap)
        } else {
            wallpaper(null, bitmap)
        }
    }

    @SuppressLint("NewApi")
    private fun wallpaper(which: Int?, bitmap: Bitmap) {
        val manager = WallpaperManager.getInstance(appContext)
        Handler().postDelayed({
            try {
                if (which != null) {
                    manager.setBitmap(bitmap, null, true, which)
                } else {
                    manager.setBitmap(bitmap)
                }
            } catch (e: Exception) {
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") Log.e("IOException", e.localizedMessage)
            }
        }, 500)
    }

    private fun getImage(listing: RedditFetch.RedditChildrenData, thumbnail: Boolean = false): String {
        val quality = Preferences().getImageQuality(appContext)
        val resolutions = listing.preview?.images?.get(0)?.resolutions
        val image = resolutions?.get(if (quality && !thumbnail) resolutions.lastIndex else 1)
        return if (!image?.url.isNullOrEmpty()) {
            return fixUrl(image?.url.toString())
        } else if (thumbnail) {
            listing.thumbnail
        } else {
            listing.url
        }
    }

    private fun fixUrl(url: String): String {
        return url.replace("amp;", "")
    }

    private fun createNotification(message: String) {
        val anotherIntent = Intent(appContext, MyBroadcastReceiver::class.java).apply {
            // TODO
        }
        val anotherPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, anotherIntent, 0)
        val title = appContext.getString(R.string.notification_title)
        val another = appContext.getString(R.string.another)
        var builder = NotificationCompat.Builder(appContext, "PLASTR")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title).setContentText(message).setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_snooze, another, anotherPendingIntent)
        with(NotificationManagerCompat.from(appContext)) {
            notify(0, builder.build())
        }
    }
}