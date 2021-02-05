package com.expert.qrgenerator.activities

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

open class BaseActivity : AppCompatActivity() {

    companion object{

         fun generateQRCode(text: String): Bitmap {
            val width = 500
            val height = 500
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val codeWriter = MultiFormatWriter()
            try {
                val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
            } catch (e: WriterException) {
                Log.d("TEST199", "generateQRCode: ${e.message}")
            }
            return bitmap
        }

        fun generateQRCode(text: String,color:String): Bitmap {
            val width = 500
            val height = 500
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val codeWriter = MultiFormatWriter()
            try {
                val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.parseColor("#$color") else Color.WHITE)
                    }
                }
            } catch (e: WriterException) {
                Log.d("TEST199", "generateQRCode: ${e.message}")
            }
            return bitmap
        }

    }

}