package com.cymbit.plastr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cymbit.plastr.helpers.DownloadImageTask
import com.cymbit.plastr.service.RedditFetch
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.iconics.utils.setIconicsFactory
import kotlinx.android.synthetic.main.activity_image.*
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.squareup.picasso.Picasso
import java.lang.Exception
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import java.text.NumberFormat
import java.util.*

class ImageActivity : AppCompatActivity() {
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.setIconicsFactory(delegate)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val listing = intent.extras?.get("LISTING_DATA") as? RedditFetch.RedditChildrenData

        if (listing != null) {
            Picasso.get().load(listing.url).into(object: com.squareup.picasso.Target {
                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) { showErrorDialog() }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                override fun onBitmapLoaded(_bitmap: Bitmap, from: Picasso.LoadedFrom?) {
                    bitmap = _bitmap
                    image.setImageBitmap(bitmap)
                    if (bitmap.width > 0 && bitmap.height > 0) {

                    }
                }
            })
            image_title.text = listing.title.toUpperCase()
            author.text = listing.author.toUpperCase()
            sub_info.text = fromHtml("<a href=\"http://www.reddit.com/r/" + listing.subreddit + "\">/r/" + listing.subreddit + "</a>")
            sub_info.movementMethod = LinkMovementMethod.getInstance()
            domain.text = fromHtml("<a href=\"http://www.reddit.com" + listing.permalink + "\">Reddit Link</a>")
            domain.movementMethod = LinkMovementMethod.getInstance()
            score.text = NumberFormat.getNumberInstance(Locale.US).format(listing.score)
            comment.text = NumberFormat.getNumberInstance(Locale.US).format(listing.num_comments)
        }

        back.setOnClickListener { finish() }
        save_image.setOnClickListener { v ->
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission == PackageManager.PERMISSION_GRANTED) {
                DownloadImageTask(this).execute(listing!!.url, listing.id)
                Snackbar.make(v, R.string.save_success, Snackbar.LENGTH_SHORT).show()
            } else {
                this.permissionSnackBar(v).show()
            }
        }

        favorite_container.setOnClickListener { favorite.performClick() }
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
        val builder = AlertDialog.Builder(this)
        with(builder) {
            setTitle(R.string.bitmap_error_title)
            setMessage(R.string.bitmap_error_content)
            setPositiveButton(R.string.back) { _, _ -> finish() }
            show()
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
}