package com.cymbit.plastr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cymbit.plastr.helpers.DownloadImageTask
import com.cymbit.plastr.service.RedditFetch
import com.mikepenz.iconics.utils.setIconicsFactory
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        layoutInflater.setIconicsFactory(delegate)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val listing = intent.extras?.get("LISTING_DATA") as? RedditFetch.RedditChildrenData
        image_title.text = listing?.title?.toUpperCase()
        author.text = listing?.author?.toUpperCase()


        save_image.setOnClickListener { v ->
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission == PackageManager.PERMISSION_GRANTED) {
                DownloadImageTask(this).execute(listing!!.preview.images[0].source.url)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
    }
}