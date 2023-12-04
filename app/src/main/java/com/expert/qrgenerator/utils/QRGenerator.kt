package com.expert.qrgenerator.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.background.StillBackground
import com.github.sumimakito.awesomeqr.option.logo.Logo

class QRGenerator {

    companion object{
        private var previousBackgroundImage: Bitmap? = null
        private var previousColor: Int? = null
        private var previousLogo: Bitmap? = null

        //THIS FUNCTION WILL GENERATE THE FINAL QR IMAGE
        fun generatorQRImage(context: Context,
                             text: String,
                             col: String,
                             bgImage: String,
                             logoUrl: String):Bitmap?{

            val renderOption = RenderOption()

            renderOption.content = text // content to encode
            renderOption.size = 800 // size of the final QR code image
            renderOption.borderWidth = 20 // width of the empty space around the QR code
            renderOption.patternScale = 0.45f // (optional) specify a scale for patterns
            renderOption.roundedPatterns = false // (optional) if true, blocks will be drawn as dots instead
            renderOption.clearBorder = true // if set to true, the background will NOT be drawn on the border area
            val color = com.github.sumimakito.awesomeqr.option.color.Color()
            color.background = 0xFFFFFFFF.toInt()

            if (col.isNotEmpty()) {
                previousColor = Color.parseColor("#$col")
                color.dark = previousColor!!
                renderOption.color = color // set a color palette for the QR code
            } else {
                if (previousColor != null) {
                    color.dark = previousColor!!
                    renderOption.color = color
                }
            }
            val background = StillBackground()
            if (bgImage.isNotEmpty()) {
                previousBackgroundImage = ImageManager.getBitmapFromURL(context, bgImage)
                background.bitmap = previousBackgroundImage
                renderOption.background = background // set a background
            } else {
                if (previousBackgroundImage != null) {
                    background.bitmap = previousBackgroundImage
                    renderOption.background = background // set a background
                }
            }

            val logo = Logo()
            if (logoUrl.isNotEmpty()) {
                previousLogo = ImageManager.getBitmapFromURL(context, logoUrl)
                logo.bitmap = previousLogo
                renderOption.logo = logo // set a logo
            } else {
                if (previousLogo != null) {
                    logo.bitmap = previousLogo
                    renderOption.logo = logo // set a logo
                }
            }

            return try {
                val result = AwesomeQrRenderer.render(renderOption)
                if (result.bitmap != null) {
                    // play with the bitmap
                    result.bitmap!!
                } else {
                    // Oops, something gone wrong.
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Oops, something gone wrong.
                null
            }

        }

        fun resetQRGenerator(){
            previousBackgroundImage = null
            previousColor = null
            previousLogo = null
        }
    }
}