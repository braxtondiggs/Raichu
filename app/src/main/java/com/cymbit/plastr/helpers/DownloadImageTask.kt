package com.cymbit.plastr.helpers

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

class DownloadImageTask(context: Context) : AsyncTask<String, Unit, Unit>() {
    private var mContext: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(vararg params: String?) {
        val url = params[0]
        val id = params[1]

        mContext.get()?.let {
            val bitmap = Picasso.get()
                .load(url)
                .get()

            try {
                @Suppress("DEPRECATION")
                val directory = File(Environment.getExternalStorageDirectory().toString() + "/Plastr/Wallpapers")
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, id.plus(".png"))
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()
            } catch (e: Exception) {
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                Log.e("IOException", e.localizedMessage)
            }
        }
    }
}