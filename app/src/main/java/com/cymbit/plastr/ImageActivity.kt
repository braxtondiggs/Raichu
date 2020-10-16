package com.cymbit.plastr

import android.Manifest
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.request.transition.Transition
import com.cymbit.plastr.helpers.Firebase
import com.cymbit.plastr.helpers.Preferences
import com.cymbit.plastr.helpers.Utils
import com.cymbit.plastr.service.RedditFetch
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.mikepenz.iconics.utils.setIconicsFactory
import com.varunest.sparkbutton.SparkEventListener
import kotlinx.android.synthetic.main.activity_image.*
import kotlinx.android.synthetic.main.dialog_set_image.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import org.jetbrains.anko.displayMetrics
import java.io.ByteArrayOutputStream

class ImageActivity : AppCompatActivity() {
    private var background: Int? = null
    private lateinit var bitmap: Bitmap
    private lateinit var originalBitmap: Bitmap
    private lateinit var tempPath: String
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var imageWidth: Int? = 0
    private var imageHeight: Int? = 0
    private var screenHeight: Int = 0
    private var screenWidth: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.setIconicsFactory(delegate)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val fb = Firebase()
        val db = FirebaseFirestore.getInstance()
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        val listing = intent.extras!!.get("LISTING_DATA") as RedditFetch.RedditChildrenData
        val bundle = intent.extras
        if (bundle?.get("COLOR") != null) background = bundle.get("COLOR") as Int
        db.document("favorites/" + fb.auth.currentUser?.uid + listing.id).addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            favorite.isChecked = snapshot != null && snapshot.exists()
        }

        val sdf = SimpleDateFormat("MM/dd/yy", Locale.ENGLISH)
        val snackbar = Snackbar.make(image, "", 1)
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        snackbar.show()
        Glide.with(this).load(getImage(listing)).error(R.mipmap.ic_launcher_foreground).thumbnail(
            Glide.with(this).load(getImage(listing, true)).apply(RequestOptions()).centerCrop()).transition(
            withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build())).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                showErrorDialog()
                return false
            }

            override fun onResourceReady(resource: Drawable, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                bitmap = resource.toBitmap()
                if (imageHeight!! > 0 && imageWidth!! > 0) {
                    dimension.text = getString(R.string.size, imageWidth, imageHeight)
                } else {
                    row2.visibility = View.GONE // TODO: Fix image size and dimension
                }
                if (background == null) {
                    Palette.Builder(resource.toBitmap()).generate {
                        it?.let { p ->
                            background = p.getDominantColor(ContextCompat.getColor(applicationContext, R.color.initial_background))
                            window.statusBarColor = background as Int
                        }
                    }
                }
                // if (bitmap.width > 0 && bitmap.height > 0) size.text = getString(R.string.size, bitmap.width, bitmap.height)
                // if (bitmap.byteCount > 0) dimension.text = getString(R.string.label, "SIZE", Utils().humanReadableByteCount(bitmap.size(), true))
                return false
            }

        }).centerCrop().into(image)
        Glide.with(this).load(getImage(listing)).into(object : CustomTarget<Drawable>() {
            override fun onLoadCleared(placeholder: Drawable?) {}

            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                originalBitmap = resource.toBitmap()
            }
        })
        image_title.text = Utils().convertEntity(listing.title).toUpperCase(Locale("US"))
        author.text = listing.author.toUpperCase(Locale("US"))
        sub_info.text = fromHtml("<a href=\"http://www.reddit.com/r/" + listing.subreddit + "\">/r/" + listing.subreddit + "</a>")
        sub_info.movementMethod = LinkMovementMethod.getInstance()
        domain.text = fromHtml("<a href=\"http://www.reddit.com" + listing.permalink + "\">Reddit URL</a>")
        domain.movementMethod = LinkMovementMethod.getInstance()
        date.text = getString(R.string.label, "CREATED", sdf.format(Date(listing.created * 1000)))
        root_domain.text = listing.domain
        back.setOnClickListener { finish() }

        save_image.setOnClickListener { v ->
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission == PackageManager.PERMISSION_GRANTED) {
                save_image_view.visibility = View.GONE
                save_text.visibility = View.GONE
                save_loading.visibility = View.VISIBLE
                Glide.with(this).asBitmap().load(listing.url).centerCrop().into(object : CustomTarget<Bitmap>() {
                    override fun onLoadCleared(placeholder: Drawable?) {}

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        if (isExternalStorageWritable()) {
                            saveImage(resource, listing.id)
                        } else {
                            Snackbar.make(container, "Could not access your external storage", Snackbar.LENGTH_SHORT).show()
                        }
                    }

                })
                Handler(Looper.getMainLooper()).postDelayed({
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
                        fb.favorite(listing, this@ImageActivity, container, getString(R.string.favorite_add))
                    } else {
                        fb.unfavorite(listing, this@ImageActivity, container, getString(R.string.favorite_remove))
                    }
                }
            }
        })

        set_image.setOnClickListener {
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission == PackageManager.PERMISSION_GRANTED) {
                if (isExternalStorageWritable()) {
                    val imageUri = getImageUriFromBitmap(originalBitmap, listing)
                    CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setRequestedSize(screenWidth, screenHeight).start(this)
                } else {
                    Snackbar.make(container, "Could not access your external storage", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                this.permissionSnackBar(it, 0).show()
            }
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(p0: View, p1: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val fraction = (slideOffset + 1f) / 2f
                val color = ArgbEvaluatorCompat.getInstance().evaluate(fraction, getColorWithAlpha(background as Int, 0.0f), background)
                bottom_sheet.setBackgroundColor(color)
            }
        }
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        if (background != null) {
            val color = ArgbEvaluatorCompat.getInstance().evaluate(0.5f, getColorWithAlpha(background as Int, 0.0f), background)
            bottom_sheet.backgroundColor = color
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            window.statusBarColor = background as Int
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            sub_container.background = ContextCompat.getDrawable(applicationContext, R.drawable.border_view_attrs)
            dimension_container.background = ContextCompat.getDrawable(applicationContext, R.drawable.border_view_attrs)
            created_container.background = ContextCompat.getDrawable(applicationContext, R.drawable.border_view_attrs)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data !== null && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                val bitmap: Bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(this.contentResolver, result.uri)
                } else {
                    val source = ImageDecoder.createSource(this.contentResolver, result.uri)
                    ImageDecoder.decodeBitmap(source)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val dialog = MaterialDialog(this).customView(R.layout.dialog_set_image).cornerRadius(16f)

                    val dialogView = dialog.getCustomView()
                    dialog.show()
                    dialogView.home.onClick {
                        dialog.dismiss()
                        setWallpaper(WallpaperManager.FLAG_SYSTEM, bitmap)
                    }

                    dialogView.lock.onClick {
                        dialog.dismiss()
                        setWallpaper(WallpaperManager.FLAG_LOCK, bitmap)
                    }

                    dialogView.home_lock.onClick {
                        dialog.dismiss()
                        setWallpaper(WallpaperManager.FLAG_SYSTEM, bitmap)
                        setWallpaper(WallpaperManager.FLAG_LOCK, bitmap)
                    }
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Snackbar.make(image, R.string.wallpaper_set, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setWallpaper(which: Int?, bitmap: Bitmap) {
        val manager = WallpaperManager.getInstance(this)
        set_image_view.visibility = View.GONE
        set_text.visibility = View.GONE
        set_loading.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (which != null) {
                    manager.setBitmap(bitmap, null, true, which)
                } else {
                    manager.setBitmap(bitmap)
                }
                Snackbar.make(image, R.string.wallpaper_set, Snackbar.LENGTH_SHORT).show()
                set_image_view.visibility = View.VISIBLE
                set_text.visibility = View.VISIBLE
                set_loading.visibility = View.GONE
            } catch (e: Exception) {
                Log.e("IOException", "LocalizedMessage of Exception : " + e.localizedMessage)
            }
        }, 500)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            this.permissionSnackBar(window.decorView.rootView, requestCode).show()
        } else if (requestCode == 1) {
            save_image.performClick()
        }
    }

    private fun permissionSnackBar(v: View, requestCode: Int = 1): Snackbar {
        return Snackbar.make(v, R.string.storage_permission, Snackbar.LENGTH_INDEFINITE).setAction(R.string.allow) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)
        }
    }

    private fun showErrorDialog() {
        MaterialDialog(this).show {
            title(R.string.bitmap_error_title)
            message(R.string.bitmap_error_content)
            positiveButton(R.string.back) { finish() }
        }
    }

    private fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html)
        }
    }

    private fun saveImage(resource: Bitmap, id: String) {
        try {
            val directory = File(Environment.getExternalStorageDirectory().toString() + "/Plastr")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, id.plus(".png"))
            val out = FileOutputStream(file)
            resource.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            Log.e("IOException", e.localizedMessage!!)
        }
    }

    private fun getColorWithAlpha(color: Int, ratio: Float): Int {
        val alpha = (Color.alpha(color) * ratio).roundToInt()
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(alpha, r, g, b)
    }

    private fun getImage(listing: RedditFetch.RedditChildrenData, thumbnail: Boolean = false): String {
        val quality = Preferences().getImageQuality(this)
        val images = listing.preview?.images
        val resolutions = if (!images.isNullOrEmpty()) images[0].resolutions else null
        val image = if (!resolutions.isNullOrEmpty()) resolutions[if (quality && !thumbnail) resolutions.lastIndex else 1] else null
        return if (!image?.url.isNullOrEmpty()) {
            if (!thumbnail) {
                imageWidth = image?.width
                imageHeight = image?.height
            }
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

    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            return true
        }
        return false
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap, listing: RedditFetch.RedditChildrenData): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        tempPath = MediaStore.Images.Media.insertImage(applicationContext.contentResolver, bitmap, listing.id, null)
        return Uri.parse(tempPath)
    }
}
