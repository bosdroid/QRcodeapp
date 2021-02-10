package com.expert.qrgenerator.utils

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException


class ImageManager {

    companion object {

        // THIS FUNCTION WILL CONVERT BITMAP IMAGE FROM VIEW
        fun loadBitmapFromView(context: Context, _view: View): Bitmap? {
            _view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val bitmap = Bitmap.createBitmap(_view.width, _view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            _view.draw(canvas)

            return bitmap
        }

        fun getImageSize(context: Context, uri: Uri?): String? {
            var fileSize: String? = null
            val cursor: Cursor? = context.contentResolver.query(uri!!, null, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {

                    // get file size
                    val sizeIndex: Int = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (!cursor.isNull(sizeIndex)) {
                        fileSize = cursor.getString(sizeIndex)
                    }
                }
            } finally {
                cursor!!.close()
            }
            return fileSize
        }

        fun getImageWidthHeight(context: Context, uri: Uri):String {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri),
                null,
                options
            )
            val imageHeight = options.outHeight
            val imageWidth = options.outWidth
            return "$imageWidth,$imageHeight"
        }
        fun getRealPathFromUri(context: Context, contentUri: Uri?): String? {
            var cursor: Cursor? = null
            return try {
                val proj = arrayOf(MediaStore.Images.Media.DATA)
                cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                cursor.getString(column_index)
            } finally {
                cursor?.close()
            }
        }

        fun getFileFromMediaUri(ac: Context, uri: Uri): File? {
            if (uri.scheme.toString().compareTo("content") == 0) {
                val cr = ac.contentResolver
                val cursor =
                    cr.query(uri, null, null, null, null) // Find from database according to Uri
                if (cursor != null) {
                    cursor.moveToFirst()
                    val filePath =
                        cursor.getString(cursor.getColumnIndex("_data")) // Get picture path
                    cursor.close()
                    if (filePath != null) {
                        return File(filePath)
                    }
                }
            } else if (uri.scheme.toString().compareTo("file") == 0) {
                return File(uri.toString().replace("file://", ""))
            }
            return null
        }

        fun getBitmapDegree(path: String?): Int {
            var degree = 0
            try {
                // Read the picture from the specified path and obtain its EXIF information
                val exifInterface = ExifInterface(path!!)
                // Get rotation information for pictures
                val orientation: Int = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return degree
        }

        fun rotateBitmapByDegree(bm: Bitmap, degree: Int): Bitmap {
            var returnBm: Bitmap? = null

            // Generate rotation matrix according to rotation angle
            val matrix = Matrix()
            matrix.postRotate(degree.toFloat())
            try {
                // Rotate the original image according to the rotation matrix and get a new image
                returnBm = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)
            } catch (e: OutOfMemoryError) {
            }
            if (returnBm == null) {
                returnBm = bm
            }
            if (bm != returnBm) {
                bm.recycle()
            }
            return returnBm
        }
    }


}