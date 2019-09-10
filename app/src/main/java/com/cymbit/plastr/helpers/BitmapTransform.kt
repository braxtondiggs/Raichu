package com.cymbit.plastr.helpers

import android.graphics.Bitmap
import com.squareup.picasso.Transformation


class BitmapTransform(private var maxWidth: Int, private var maxHeight: Int) : Transformation {
    var byteCount: Int = 0
    var width: Int = 0
    var height: Int = 0
    lateinit var bitmap: Bitmap
    override fun key(): String {
        return maxWidth.toString() + "x" + maxHeight.toString()
    }

    override fun transform(source: Bitmap): Bitmap {
        val targetWidth: Int
        val targetHeight: Int
        val aspectRatio: Double
        if (source.width > source.height) {
            targetWidth = maxWidth
            aspectRatio = source.height.toDouble() / source.width.toDouble()
            targetHeight = (targetWidth * aspectRatio).toInt()
        } else {
            targetHeight = maxHeight
            aspectRatio = source.width.toDouble() / source.height.toDouble()
            targetWidth = (targetHeight * aspectRatio).toInt()
        }

        bitmap = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
        byteCount = bitmap.byteCount
        height = bitmap.height
        width = bitmap.width
        if (bitmap != source) {
            source.recycle()
        }
        return bitmap
    }
}

