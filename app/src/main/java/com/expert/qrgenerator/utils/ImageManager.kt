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
import android.util.Log
import android.view.View
import androidx.exifinterface.media.ExifInterface
import com.expert.qrgenerator.view.activities.BaseActivity
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class ImageManager {

    companion object {

        // THIS FUNCTION WILL CONVERT BITMAP IMAGE FROM VIEW AND SAVE INTO LOCAL DIRECTORY
        fun loadBitmapFromView(context: Context, _view: View): File {
            _view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val bitmap = Bitmap.createBitmap(_view.width, _view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            _view.draw(canvas)

            val fileName = "final_qr_image_" + BaseActivity.getDateTimeFromTimeStamp(System.currentTimeMillis()) + ".jpg"
            val fileDir = File(context.externalCacheDir.toString(), fileName)

            try {
                val outputStream = FileOutputStream(fileDir.toString(), false)
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return fileDir
        }


        // THIS FUNCTION WILL RETURN THE IMAGE WIDTH AND HEIGHT
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

        // THIS FUNCTION WILL RETURN THE IMAGE LOCAL URI
        private fun getRealPathFromUri(context: Context, contentUri: Uri?): String? {
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

        // THIS FUNCTION WILL RETURN THE SELECT IMAGE ANGLE
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

        // THIS FUNCTION WILL USE THE IMAGE ROTATION
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

        // THIS FUNCTION WILL SAVE CUSTOM SELECTED IMAGE IN LOCAL APP DIRECTORY
        fun saveImageInLocalStorage(context: Context,uri: Uri,type:String):String{
            var filePath: String? = null
            var fileName: String? = null

            if (type == "background") {
                filePath = context.externalCacheDir.toString() + "/BackgroundImages"
                fileName =
                    "qr_background_image_" + BaseActivity.getDateTimeFromTimeStamp(System.currentTimeMillis()) + ".jpg"
            } else {
                filePath = context.externalCacheDir.toString() + "/LogoImages"
                fileName =
                    "qr_logo_image_" + BaseActivity.getDateTimeFromTimeStamp(System.currentTimeMillis()) + ".png"
            }
            val dir = File(filePath)
            dir.mkdir()

            val newFile = File(dir, fileName)

            val realPath = getRealPathFromUri(context, uri)

            val selectImageBitmap = BaseActivity.getBitmapFromURL(context, realPath)
            try {
                val out = FileOutputStream(newFile)
                if (type == "background")
                {
                    selectImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                else
                {
                    selectImageBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.d("TEST199",realPath!!)
            return realPath
        }

    }


}