package com.expert.qrgenerator.utils

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.util.Base64OutputStream
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.FileProvider
import com.expert.qrgenerator.view.activities.BaseActivity
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


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

            val fileName =
                "final_qr_image_" + BaseActivity.getDateTimeFromTimeStamp(System.currentTimeMillis()) + ".jpg"
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
        fun getImageWidthHeight(context: Context, uri: Uri): String {
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
        fun getRealPathFromUri(context: Context, contentUri: Uri?): String? {
            var cursor: Cursor? = null
            return try {
                val proj = arrayOf(MediaStore.Images.Media.DATA)
                cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
                val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                cursor.getString(columnIndex)
            } finally {
                cursor?.close()
            }
        }

//        fun getFileRealPathFromUri(context: Context, contentUri: Uri?): String? {
//            var cursor: Cursor? = null
//            return try {
//                val proj = arrayOf(MediaStore.Files.FileColumns.DATA)
//                cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
//                val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
//                cursor.moveToFirst()
//                cursor.getString(columnIndex)
//            } finally {
//                cursor?.close()
//            }
//        }

        fun getPath(context: Context, uri: Uri): String? {
            var filePath: String? = null
            try {
                val wholeID = DocumentsContract.getDocumentId(uri)
                val id = wholeID.split(":".toRegex()).toTypedArray()[1]
                val column = arrayOf(MediaStore.Images.Media.DATA)
                val sel = MediaStore.Images.Media._ID + "=?"
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    column, sel, arrayOf(id), null
                ).use { cursor ->
                    if (cursor != null) {
                        val columnIndex = cursor.getColumnIndex(column[0])
                        if (cursor.moveToFirst()) {
                            filePath = cursor.getString(columnIndex)
                        }
                    }
                }
            } catch (ignored: java.lang.Exception) {
            }
            return filePath
        }

        // THIS FUNCTION WILL SAVE CUSTOM SELECTED IMAGE IN LOCAL APP DIRECTORY
        fun saveImageInLocalStorage(context: Context, uri: Uri, type: String): String {
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

            val selectImageBitmap = getBitmapFromURL(context, realPath)
            try {
                val out = FileOutputStream(newFile)
                if (type == "background") {
                    selectImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
                } else {
                    selectImageBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.d("TEST199", realPath!!)
            return realPath
        }

        // THIS FUNCTION WILL DOWNLOAD IMAGE FROM URL AND CONVERT INTO BITMAP FOR BACKGROUND
        fun getBitmapFromURL(context: Context, src: String?): Bitmap? {
            if (src!!.contains("http") || src.contains("https")) {
                return try {
                    val url = URL(src)
                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val input: InputStream = connection.inputStream
                    BitmapFactory.decodeStream(input)
                } catch (e: IOException) {
                    // Log exception
                    null
                }
            } else {

                return try {
                    val input: InputStream = context.contentResolver.openInputStream(
                        Uri.fromFile(
                            File(
                                src
                            )
                        )
                    )!!
                    val bitmap = BitmapFactory.decodeStream(input)
                    input.close()
                    return bitmap
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
            }
        }


        // THIS FUNCTION WILL CONVERT THE DRAWABLE IMAGE TO BITMAP (I KEEP THIS FOR FUTURE USE)
//        private fun getDrawableToBitmap(context: Context, image: Int): Bitmap {
//
//            return BitmapFactory.decodeResource(
//                context.resources,
//                image
//            )
//        }

        // THIS FUNCTION WILL GET ALL THE BACKGROUND IMAGE THAT USER HAVE SELECTED FROM EXTERNAL STORAGE
        fun getFilesFromBackgroundImagesFolder(dir: File): MutableList<String> {
            val fileList = mutableListOf<String>()
            val listFile = dir.listFiles()
            if (listFile != null && listFile.isNotEmpty()) {
                for (file in listFile) {
                    if (file.isDirectory) {
                        getFilesFromBackgroundImagesFolder(file)
                    } else {
                        if (file.name.endsWith(".jpg")) {
                            fileList.add(file.absolutePath)
                        }
                    }
                }
            }
            return fileList
        }

        // THIS FUNCTION WILL GET ALL THE LOGO IMAGE THAT USER HAVE SELECTED FROM EXTERNAL STORAGE
        fun getFilesFromLogoFolder(dir: File): MutableList<String> {
            val fileList = mutableListOf<String>()
            val listFile = dir.listFiles()
            if (listFile != null && listFile.isNotEmpty()) {
                for (file in listFile) {
                    if (file.isDirectory) {
                        getFilesFromLogoFolder(file)
                    } else {
                        if (file.name.endsWith(".png")) {
                            fileList.add(file.absolutePath)
                        }
                    }
                }
            }
            return fileList
        }

        // THIS FUNCTION WILL SAVE THE CUSTOM COLOR FILE
        fun writeColorValueToFile(data: String, context: Context) {
            try {
                val outputStreamWriter = OutputStreamWriter(
                    context.openFileOutput(
                        "color.txt",
                        Context.MODE_APPEND
                    )
                )
                outputStreamWriter.write(data)
                outputStreamWriter.close()
            } catch (e: IOException) {
                Log.e("Exception", "File write failed: $e")
            }
        }

        // THIS FUNCTION WILL READ THE CUSTOM COLOR FILE
        fun readColorFile(context: Context): String {
            var ret = ""
            try {
                val inputStream: InputStream? = context.openFileInput("color.txt")
                if (inputStream != null) {
                    val inputStreamReader = InputStreamReader(inputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)
                    var receiveString: String? = ""
                    val stringBuilder = StringBuilder()
                    while (bufferedReader.readLine().also { receiveString = it } != null) {
                        stringBuilder.append(receiveString)
                    }
                    inputStream.close()
                    ret = stringBuilder.toString()
                }
            } catch (e: FileNotFoundException) {
                Log.e("login activity", "File not found: $e")
            } catch (e: IOException) {
                Log.e("login activity", "Can not read file: $e")
            }
            return ret
        }

        // THIS FUNCTION WILL SAVE AND SHARE THE FINAL QR IMAGE GETTING FROM CACHE DIRECTORY
        fun shareImage(context: Context, qrImageWrapperLayout: RelativeLayout): Uri {

            val finalQRImageFile = loadBitmapFromView(context, qrImageWrapperLayout)

            // HERE START THE SHARE INTENT
//            val waIntent = Intent(Intent.ACTION_SEND)
            val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    context.applicationContext.packageName + ".fileprovider", finalQRImageFile
                )

            } else {
                Uri.fromFile(finalQRImageFile)
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                waIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//            if (imageUri != null) {
//                waIntent.type = "image/*"
//                waIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
//                context.startActivity(Intent.createChooser(waIntent, "Share with"))
//            }
            return imageUri

        }

        fun shareImage(context: Context, imageUri: Uri?) {
            val waIntent = Intent(Intent.ACTION_SEND)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                waIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (imageUri != null) {
                waIntent.type = "image/*"
                waIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                context.startActivity(Intent.createChooser(waIntent, "Share with"))
            }
        }


        // THIS FUNCTION WILL CONVERT THE IMAGE INTO BASE64 STRING
        fun convertImageToBase64(context: Context, uri: Uri): String {
            return FileInputStream(getRealPathFromUri(context, uri)).use { inputStream ->
                ByteArrayOutputStream().use { outputStream ->
                    Base64OutputStream(outputStream, Base64.NO_WRAP).use { base64FilterStream ->
                        inputStream.copyTo(base64FilterStream)
                        base64FilterStream.close()
                        outputStream.toString()
                    }
                }
            }
        }

        // THIS FUNCTION WILL EXTRACT THE DATA FROM QR CODE IMAGE WITHOUT CAMERA
        fun getTextFromQRImage(context: Context, bMap: Bitmap):String{
            val source = RGBLuminanceSource(bMap)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            try {
                val result: Result = reader.decode(bitmap)
//                val contents: String = result.text
//                val rawBytes: ByteArray = result.getRawBytes()
//                val format: BarcodeFormat = result.getBarcodeFormat()
//                val points: Array<ResultPoint> = result.getResultPoints()
                return result.text
            } catch (e: NotFoundException) {
                e.printStackTrace()
                return ""
            } catch (e: ChecksumException) {
                e.printStackTrace()
                return ""
            } catch (e: FormatException) {
                e.printStackTrace()
                return ""
            }
        }

        // THIS FUNCTION WILL GENERATE THE BARCODE AND RETURN AS A BITMAP IMAGE
        fun generateBarcode(encodedText: String):Bitmap?{
            val multiFormatWriter = MultiFormatWriter()
            try {
                val bitMatrix = multiFormatWriter.encode(
                    encodedText,
                    BarcodeFormat.CODE_128,
                    400,
                    200
                )

                val bitmap = Bitmap.createBitmap(
                    400,
                    200,
                    Bitmap.Config.RGB_565
                )
                for (i in 0 until 400) {
                    for (j in 0 until 200) {
                        bitmap.setPixel(i, j, if (bitMatrix[i, j]) Color.BLACK else Color.WHITE)
                    }
                }
                return bitmap
            } catch (e: WriterException) {
                e.printStackTrace()
                return null
            }
        }

        fun readWriteImage(context: Context, bitmap: Bitmap): File {
            // store in DCIM/Camera directory
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)//Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val cameraDir = File(dir, "Camera/")

            val file = if (cameraDir.exists()) {
                File(cameraDir, "JPEG_${System.currentTimeMillis()}.jpg")
            } else {
                cameraDir.mkdir()
                File(cameraDir, "JPEG_${System.currentTimeMillis()}.jpg")
            }

            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()

            return file
        }

    }


}