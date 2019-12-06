package com.cymbit.plastr.helpers

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bumptech.glide.Glide
import com.cymbit.plastr.ImageActivity
import com.cymbit.plastr.MainActivity
import com.cymbit.plastr.R
import com.cymbit.plastr.service.BaseRepository
import com.cymbit.plastr.service.RedditFactory
import com.cymbit.plastr.service.RedditFetch
import com.cymbit.plastr.service.RedditRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.*

class Worker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val repository: RedditRepository = RedditRepository(RedditFactory.redditApi)
    private var after: String? = null

    override suspend fun doWork(): Result = coroutineScope {
        val d = async { getWallpaper() }
        d.await()
    }

    private suspend fun getWallpaper(): Result {
        val pref = Preferences()
        val networkPref = pref.getNetwork(context)
        val notification = pref.getNotification(context)
        val network = checkNetworkConnection()
        if (network != null) {
            if (network == networkPref) {
                val nsfw = if (pref.getNSFW(context)) "1" else "0"
                when (val result = repository.getListings(pref.getSelectedSubs(context).joinToString("+"), pref.getSort(context), pref.getTime(context), after, nsfw)) {
                    is BaseRepository.Result.Success -> {
                        val data = result.data.data
                        val item = getItem(data.children)
                        val image = getImage(item!!.data)
                        after = data.after
                        val bitmap = Glide.with(context).asBitmap().load(image).submit().get()
                        setWallpaper(bitmap)
                        val cal = Calendar.getInstance()
                        val frequency = Constants.FREQUENCY_NUMBERS[pref.getFrequency(context)]
                        cal.add(Calendar.MINUTE, frequency.toInt())
                        pref.setBaseTime(context, cal.time)

                        if (notification) createNotification(item.data)
                        return Result.success()

                    }
                    is BaseRepository.Result.Error -> {
                        return Result.retry()
                    }
                }
            } else {
                return Result.failure()
            }
        } else {
            return Result.failure()
        }
    }

    @Suppress("DEPRECATION")
    private fun checkNetworkConnection(): Int? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
        val screen = Preferences().getApplyScreen(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (screen == 2 || screen == 0) {
                wallpaper(WallpaperManager.FLAG_SYSTEM, bitmap)
            } else if (screen == 2 || screen == 1) {
                wallpaper(WallpaperManager.FLAG_LOCK, bitmap)
            }
        } else {
            wallpaper(null, bitmap)
        }
    }

    @SuppressLint("NewApi")
    private fun wallpaper(which: Int?, bitmap: Bitmap) {
        val manager = WallpaperManager.getInstance(context)
        if (which != null) {
            manager.setBitmap(bitmap, null, true, which)
        } else {
            manager.setBitmap(bitmap)
        }
    }

    private fun getImage(listing: RedditFetch.RedditChildrenData, thumbnail: Boolean = false): String {
        val quality = Preferences().getImageQuality(context)
        val images = listing.preview?.images
        val resolutions = if (!images.isNullOrEmpty()) images[0].resolutions else null
        val image = if (!resolutions.isNullOrEmpty()) resolutions[if (quality && !thumbnail) resolutions.lastIndex else 1] else null
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

    private fun createNotification(data: RedditFetch.RedditChildrenData) {
        val channelId = "${context.packageName}-${context.getString(R.string.app_name)}"
        val title = context.getString(R.string.notification_title)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = data.title
            channel.setShowBadge(false)

            // Register the channel with the system
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
        val notificationBuilder = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.ic_notification)
            setContentTitle(title)
            setContentText(data.title)
            setAutoCancel(true)
            // addAction(R.drawable.ic_notification, context.getString(R.string.another), createAnotherIntentForAction())
            addAction(R.drawable.ic_notification, context.getString(R.string.view), createViewIntentForAction(data))
            priority = NotificationCompat.PRIORITY_DEFAULT

            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            setContentIntent(pendingIntent)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1001, notificationBuilder.build())
    }

    private fun getItem(children: List<RedditFetch.RedditChildren>): RedditFetch.RedditChildren? {
        val pref = Preferences()
        val history = pref.getImageHistory(context)
        children.forEach { child ->
            val match = history.find { it == child.data.id }
            if (history.isNullOrEmpty() || match == null) {
                pref.setHistory(context, history, child.data.id)
                return child
            }
        }
        return null
    }

    /*private fun createAnotherIntentForAction(): PendingIntent? {
        val receiver = object : Service() {
            override fun onBind(intent: Intent?): IBinder? {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        val anotherIntent = Intent(context, receiver::class.java)
        return PendingIntent.getService(context, ADMINISTER_REQUEST_CODE, anotherIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }*/

    private fun createViewIntentForAction(data: RedditFetch.RedditChildrenData): PendingIntent? {
        val viewIntent = Intent(context, ImageActivity::class.java).apply {
            putExtra("LISTING_DATA", data)
        }
        return PendingIntent.getActivity(context, ADMINISTER_REQUEST_CODE, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        private const val ADMINISTER_REQUEST_CODE = 2019
    }
}