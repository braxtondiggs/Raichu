package com.cymbit.plastr.helpers

import android.content.Context
import androidx.core.text.HtmlCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import kotlin.math.ln
import kotlin.math.pow

class Utils {
    fun humanReadableByteCount(bytes: Int, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format(Locale.ENGLISH, "%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

    fun setupWorkerManager(context: Context) {
        val tag = "PLASTR_WORKER"
        val frequency = Constants.FREQUENCY_NUMBERS[Preferences().getFrequency(context)]
        if (frequency != 0L && !WorkManager.getInstance(context).isAnyWorkScheduled(tag)) {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val saveRequest = PeriodicWorkRequestBuilder<Worker>(frequency, TimeUnit.MINUTES).addTag(tag).setConstraints(constraints).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.KEEP, saveRequest)
        } else {
            WorkManager.getInstance(context).cancelAllWork()
        }
    }

    private fun WorkManager.isAnyWorkScheduled(tag: String): Boolean {
        return try {
            getWorkInfosByTag(tag).get().firstOrNull { !it.state.isFinished } != null
        } catch (e: Exception) {
            when (e) {
                is ExecutionException, is InterruptedException -> {
                    e.printStackTrace()
                }
                else -> throw e
            }
            false
        }
    }

    fun convertEntity(value: String): String {
        return HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }
}