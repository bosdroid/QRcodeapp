package com.expert.qrgenerator.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.zxing.LuminanceSource
import java.io.FileNotFoundException

class RGBLuminanceSource(bitmap: Bitmap) : LuminanceSource(bitmap.width, bitmap.height) {
    private val luminances: ByteArray

    constructor(path: String) : this(loadBitmap(path)) {}

    override fun getRow(y: Int, row: ByteArray): ByteArray {
        var row: ByteArray? = row
        require(!(y < 0 || y >= height)) { "Requested row is outside the image: $y" }
        val width = width
        if (row == null || row.size < width) {
            row = ByteArray(width)
        }
        System.arraycopy(luminances, y * width, row, 0, width)
        return row
    }

    // Since this class does not support cropping, the underlying byte array
    // already contains
    // exactly what the caller is asking for, so give it to them without a copy.
    override fun getMatrix(): ByteArray {
        return luminances
    }

    companion object {
        @Throws(FileNotFoundException::class)
        private fun loadBitmap(path: String): Bitmap {
            return BitmapFactory.decodeFile(path)
                ?: throw FileNotFoundException("Couldn't open $path")
        }
    }

    init {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        // In order to measure pure decoding speed, we convert the entire image
        // to a greyscale array
        // up front, which is the same as the Y channel of the
        // YUVLuminanceSource in the real app.
        luminances = ByteArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                val pixel = pixels[offset + x]
                val r = pixel shr 16 and 0xff
                val g = pixel shr 8 and 0xff
                val b = pixel and 0xff
                if (r == g && g == b) {
                    // Image is already greyscale, so pick any channel.
                    luminances[offset + x] = r.toByte()
                } else {
                    // Calculate luminance cheaply, favoring green.
                    luminances[offset + x] = (r + g + g + b shr 2).toByte()
                }
            }
        }
    }
}