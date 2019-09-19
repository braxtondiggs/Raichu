package com.cymbit.plastr

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.cymbit.plastr.helpers.Firebase
import com.cymbit.plastr.service.RedditFetch
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.mikepenz.iconics.utils.setIconicsFactory
import com.varunest.sparkbutton.SparkEventListener
import kotlinx.android.synthetic.main.activity_image.*
import kotlinx.android.synthetic.main.dialog_set_image.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ln
import kotlin.math.pow

@Suppress("DEPRECATION")
class ImageActivity : AppCompatActivity() {
    private lateinit var bitmap: Bitmap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.setIconicsFactory(delegate)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val db = FirebaseFirestore.getInstance()
        val listing = intent.extras!!.get("LISTING_DATA") as RedditFetch.RedditChildrenData
        val fb = Firebase()
        db.document("favorites/" + fb.auth.currentUser?.uid + listing.id)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                favorite.isChecked = snapshot != null && snapshot.exists()
            }

        val sdf = SimpleDateFormat("MM/dd/YY", Locale.ENGLISH)
        val displayMetrics = DisplayMetrics()
        val snackbar = Snackbar.make(image, "", 1)
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        snackbar.show()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        Glide.with(this).load(listing.url).error(R.mipmap.ic_launcher_foreground)
            .thumbnail(
                Glide.with(this).load(listing.thumbnail).apply(
                    RequestOptions()
                ).override(width, height).centerCrop()
            ).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                showErrorDialog()
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                bitmap = resource.toBitmap()
                if (bitmap.width > 0 && bitmap.height > 0) size.text =
                    getString(R.string.size, bitmap.width, bitmap.height)
                if (bitmap.byteCount > 0) dimension.text = getString(
                    R.string.label,
                    "SIZE",
                    humanReadableByteCount(bitmap.byteCount, true)
                )
                return false
            }

        }).override(width, height).centerCrop().into(image)
        image_title.text = listing.title.toUpperCase(Locale("US"))
        author.text = listing.author.toUpperCase(Locale("US"))
        sub_info.text =
            fromHtml("<a href=\"http://www.reddit.com/r/" + listing.subreddit + "\">/r/" + listing.subreddit + "</a>")
        sub_info.movementMethod = LinkMovementMethod.getInstance()
        domain.text =
            fromHtml("<a href=\"http://www.reddit.com" + listing.permalink + "\">Reddit URL</a>")
        domain.movementMethod = LinkMovementMethod.getInstance()
        date.text = getString(R.string.label, "CREATED", sdf.format(Date(listing.created * 1000)))
        root_domain.text = listing.domain

        back.setOnClickListener { finish() }

        save_image.setOnClickListener { v ->
            val permission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission == PackageManager.PERMISSION_GRANTED) {
                save_image_view.visibility = View.GONE
                save_text.visibility = View.GONE
                save_loading.visibility = View.VISIBLE
                Glide.with(this).asBitmap().load(listing.url).centerCrop()
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onLoadCleared(placeholder: Drawable?) {}

                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            saveImage(resource, listing.id)
                        }

                    })
                Handler().postDelayed({
                    Snackbar.make(v, R.string.save_success, Snackbar.LENGTH_SHORT).show()
                    save_image_view.visibility = View.VISIBLE
                    save_text.visibility = View.VISIBLE
                    save_loading.visibility = View.GONE
                }, 500)
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
                    if (buttonState) {
                        fb.favorite(
                            listing,
                            this@ImageActivity,
                            container,
                            getString(R.string.favorite_add)
                        )
                    } else {
                        fb.unfavorite(
                            listing,
                            this@ImageActivity,
                            container,
                            getString(R.string.favorite_remove)
                        )
                    }
                }
            }
        })

        set_image.setOnClickListener { v ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val dialog =
                    MaterialDialog(this).customView(R.layout.dialog_set_image).cornerRadius(16f)

                val dialogView = dialog.getCustomView()
                dialog.show()
                dialogView.home.onClick {
                    dialog.dismiss()
                    setWallpaper(WallpaperManager.FLAG_SYSTEM, v)
                }

                dialogView.lock.onClick {
                    dialog.dismiss()
                    setWallpaper(WallpaperManager.FLAG_LOCK, v)
                }

                dialogView.home_lock.onClick {
                    dialog.dismiss()
                    setWallpaper(WallpaperManager.FLAG_SYSTEM, v)
                    setWallpaper(WallpaperManager.FLAG_LOCK, v)
                }
            } else {
                setWallpaper(null, v)
            }
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(p0: View, p1: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val fraction = (slideOffset + 1f) / 2f
                val color = ArgbEvaluatorCompat.getInstance().evaluate(
                    fraction,
                    resources.getColor(R.color.initial_background),
                    resources.getColor(R.color.colorPrimaryDark)
                )
                bottom_sheet.setBackgroundColor(color)
            }
        }
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback)
        val color = ArgbEvaluatorCompat.getInstance().evaluate(
            0.5f,
            resources.getColor(R.color.initial_background),
            resources.getColor(R.color.colorPrimaryDark)
        )
        bottom_sheet.setBackgroundColor(color)

    }

    @SuppressLint("NewApi")
    private fun setWallpaper(which: Int?, v: View) {
        val manager = WallpaperManager.getInstance(this)
        set_image_view.visibility = View.GONE
        set_text.visibility = View.GONE
        set_loading.visibility = View.VISIBLE
        Handler().postDelayed({
            try {
                if (which != null) {
                    manager.setBitmap(bitmap, null, true, which)
                } else {
                    manager.setBitmap(bitmap)
                }
                Snackbar.make(v, R.string.wallpaper_set, Snackbar.LENGTH_SHORT).show()
                set_image_view.visibility = View.VISIBLE
                set_text.visibility = View.VISIBLE
                set_loading.visibility = View.GONE
            } catch (e: Exception) {
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                Log.e("IOException", e.localizedMessage)
            }
        }, 500)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            this.permissionSnackBar(window.decorView.rootView).show()
        } else {
            save_image.performClick()
        }
    }

    private fun permissionSnackBar(v: View): Snackbar {
        return Snackbar.make(v, R.string.storage_permission, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.allow) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
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
        return String.format(
            Locale.ENGLISH,
            "%.1f %sB",
            bytes / unit.toDouble().pow(exp.toDouble()),
            pre
        )
    }

    private fun saveImage(resource: Bitmap, id: String) {
        try {
            @Suppress("DEPRECATION") val directory =
                File(Environment.getExternalStorageDirectory().toString() + "/Plastr/Wallpapers")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, id.plus(".png"))
            val out = FileOutputStream(file)
            resource.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            Log.e("IOException", e.localizedMessage)
        }
    }
}
