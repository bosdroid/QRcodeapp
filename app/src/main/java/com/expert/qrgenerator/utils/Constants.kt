package com.expert.qrgenerator.utils

import android.content.Context
import android.util.Log
import java.io.*

class Constants {

    // HERE WE WILL CREATE ALL THE CONSTANT DATA
    companion object {
        const val firebaseBackgroundImages = "backgroundImages"
        const val firebaseLogoImages = "logoImages"
        const val firebaseFonts = "fonts"
        const val READ_STORAGE_REQUEST_CODE = 100
        const val READ_STORAGE_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE"
        const val BACKGROUND_IMAGE_PATH = "BackgroundImages"
        const val LOGO_IMAGE_PATH = "LogoImages"
        const val COLOR_PATH = "Colors"

        private fun getBackgroundImageFolderFile(context: Context): File {
            return File(context.externalCacheDir, BACKGROUND_IMAGE_PATH)
        }

        private fun getLogoImageFolderFile(context: Context): File {
            return File(context.externalCacheDir, LOGO_IMAGE_PATH)
        }


        // THIS FUNCTION WILL RETURN THE ALL THE EXTERNAL BACKGROUND IMAGES
        fun getAllBackgroundImages(context: Context): List<String> {
            return getFilesFromFolder(getBackgroundImageFolderFile(context))
        }

        // THIS FUNCTION WILL RETURN THE ALL THE EXTERNAL LOGO IMAGES
        fun getAllLogoImages(context: Context): List<String> {
            return getFilesFromFolder(getLogoImageFolderFile(context))
        }

        // THIS FUNCTION WILL GET ALL THE BACKGROUND/LOGO IMAGE THAT USER HAVE SELECTED FROM EXTERNAL STORAGE
        private fun getFilesFromFolder(dir: File): MutableList<String> {
            val fileList = mutableListOf<String>()
            val listFile = dir.listFiles()
            if (listFile != null && listFile.isNotEmpty()) {
                for (file in listFile) {
                    if (file.isDirectory) {
                        getFilesFromFolder(file)
                    } else {
                        if (file.name.endsWith(".jpg")) {
                            fileList.add(file.absolutePath)
                        }
                    }
                }
            }
            return fileList
        }

        // THIS FUNCTION WILL SAVE THE CUSTOM COLOR FILE
        fun writeColorValueToFile(data: String, context: Context) {
//             val dir = File(context.externalCacheDir, COLOR_PATH)
//             dir.mkdir()
//             val colorFile = File(dir, "color.txt")
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
//            val dir = File(context.externalCacheDir, COLOR_PATH)
//            val colorFile = File(dir, "color.txt")
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
                Log.e("login activity", "File not found: " + e.toString())
            } catch (e: IOException) {
                Log.e("login activity", "Can not read file: $e")
            }
            return ret
        }
    }
}