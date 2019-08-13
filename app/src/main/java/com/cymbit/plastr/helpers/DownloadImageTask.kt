package com.cymbit.plastr.helpers

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

class DownloadImageTask(context: Context) : AsyncTask<String, Unit, Unit>() {
    private var mContext: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(vararg params: String?) {
        val url = params[0]

        mContext.get()?.let {
            val bitmap = Picasso.get()
                .load(url)
                .get()

            try {
                var file = it.getDir("Images", Context.MODE_PRIVATE)
                file = File(file, "img.jpg")
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                out.flush()
                out.close()
                Log.i("Seiggailion", "Image saved.")
            } catch (e: Exception) {
                Log.i("Seiggailion", "Failed to save image.")
            }
        }
    }
}