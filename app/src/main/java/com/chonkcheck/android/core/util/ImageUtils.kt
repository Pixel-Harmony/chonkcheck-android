package com.chonkcheck.android.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtils {

    fun uriToBase64(context: Context, uri: Uri, maxWidth: Int = 1024, quality: Int = 85): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    ?: return null

                val scaledBitmap = scaleBitmap(originalBitmap, maxWidth)

                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

                if (scaledBitmap != originalBitmap) {
                    scaledBitmap.recycle()
                }
                originalBitmap.recycle()

                val byteArray = outputStream.toByteArray()
                Base64.encodeToString(byteArray, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int): Bitmap {
        if (bitmap.width <= maxWidth) {
            return bitmap
        }

        val scaleFactor = maxWidth.toFloat() / bitmap.width
        val newHeight = (bitmap.height * scaleFactor).toInt()

        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true)
    }
}
