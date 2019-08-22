package com.cymbit.plastr

import android.Manifest
import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.room.Room
import com.afollestad.materialdialogs.MaterialDialog
import com.cymbit.plastr.helpers.DownloadImageTask
import com.cymbit.plastr.service.FavoriteViewModel
import com.cymbit.plastr.service.RedditFetch
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.iconics.utils.setIconicsFactory
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.varunest.sparkbutton.SparkEventListener
import kotlinx.android.synthetic.main.activity_image.*
import org.jetbrains.anko.doAsync
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ln
import kotlin.math.pow

class ImageActivity : AppCompatActivity() {
    private lateinit var bitmap: Bitmap
    private var hasFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.setIconicsFactory(delegate)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "RedditChildrenData").build()
        val listing = intent.extras!!.get("LISTING_DATA") as RedditFetch.RedditChildrenData

        db.redditDao().findById(listing.id).observe(this, Observer<RedditFetch.RedditChildrenData> { t ->
            favorite.isChecked = t !== null
            hasFavorite = t !== null
        })

        val sdf = SimpleDateFormat("MM/dd/YY", Locale.ENGLISH)
        val favoriteViewModel = ViewModelProviders.of(this).get(FavoriteViewModel::class.java)
        Picasso.get().load(listing.url).into(target)
        image.tag = target
        image_title.text = listing.title.toUpperCase()
        author.text = listing.author.toUpperCase()
        sub_info.text =
            fromHtml("<a href=\"http://www.reddit.com/r/" + listing.subreddit + "\">/r/" + listing.subreddit + "</a>")
        sub_info.movementMethod = LinkMovementMethod.getInstance()
        domain.text = fromHtml("<a href=\"http://www.reddit.com" + listing.permalink + "\">Reddit URL</a>")
        domain.movementMethod = LinkMovementMethod.getInstance()
        score.text = getString(R.string.label, "SCORE", NumberFormat.getNumberInstance(Locale.US).format(listing.score))
        comment.text = getString(
            R.string.label,
            "COMMENTS",
            NumberFormat.getNumberInstance(Locale.US).format(listing.num_comments)
        )
        date.text = getString(R.string.label, "CREATED", sdf.format(Date(listing.created * 1000)))
        root_domain.text = listing.domain

        back.setOnClickListener { finish() }
        save_image.setOnClickListener { v ->
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission == PackageManager.PERMISSION_GRANTED) {
                DownloadImageTask(this).execute(listing.url, listing.id)
                Snackbar.make(v, R.string.save_success, Snackbar.LENGTH_SHORT).show()
            } else {
                this.permissionSnackBar(v).show()
            }
        }

        favorite_container.setOnClickListener { favorite.performClick() }

        favorite.setEventListener(object : SparkEventListener {
            override fun onEventAnimationStart(button: ImageView?, buttonState: Boolean) {}

            override fun onEventAnimationEnd(button: ImageView?, buttonState: Boolean) {}

            override fun onEvent(button: ImageView, buttonState: Boolean) {
                doAsync {
                    if (!hasFavorite) {
                        db.redditDao().insert(listing)
                        favoriteViewModel.insert(listing)
                        Snackbar.make(container, getString(R.string.favorite_add), Snackbar.LENGTH_SHORT).show()
                    } else {
                        db.redditDao().delete(listing)
                        favoriteViewModel.delete(listing)
                        Snackbar.make(container, getString(R.string.favorite_remove), Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        })

        set_image.setOnClickListener { v ->
            val manager = WallpaperManager.getInstance(applicationContext)
            try {
                manager.setBitmap(bitmap)
                Snackbar.make(v, R.string.wallpaper_set, Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                Log.e("IOException", e.localizedMessage)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            this.permissionSnackBar(window.decorView.rootView).show()
        } else {
            save_image.performClick()
        }
    }

    private fun permissionSnackBar(v: View): Snackbar {
        return Snackbar.make(v, R.string.storage_permission, Snackbar.LENGTH_INDEFINITE).setAction(R.string.allow) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    private fun showErrorDialog() {
        MaterialDialog(this).show {
            title(R.string.bitmap_error_title)
            message(R.string.bitmap_error_content)
            positiveButton(R.string.back) { finish() }
        }
    }

    @Suppress("DEPRECATION")
    private fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }

    private fun humanReadableByteCount(bytes: Int, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format(Locale.ENGLISH, "%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

    private var target: Target = object : Target {
        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            showErrorDialog()
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

        override fun onBitmapLoaded(_bitmap: Bitmap, from: Picasso.LoadedFrom?) {
            bitmap = _bitmap
            image.setImageBitmap(bitmap)
            if (bitmap.width > 0 && bitmap.height > 0) {
                size.text = getString(R.string.size, bitmap.width, bitmap.height)
            }
            if (bitmap.byteCount > 0) {
                dimension.text = getString(R.string.label, "SIZE", humanReadableByteCount(bitmap.byteCount, true))
            }
        }
    }
}