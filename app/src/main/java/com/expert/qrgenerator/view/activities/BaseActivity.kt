package com.expert.qrgenerator.view.activities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.expert.qrgenerator.R
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.background.StillBackground
import com.github.sumimakito.awesomeqr.option.logo.Logo
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


open class BaseActivity : AppCompatActivity() {

    companion object{
        var previoudBackgroundImage:Bitmap?=null
        var previousColor:Int?=null

        //THIS FUNCTION WILL GENERATE THE FINAL QR IMAGE
        fun generateQRWithBackgroundImage(
            context: Context,
            text: String,
            col: String,
            bgImage: String
        ):Bitmap?{
            val renderOption = RenderOption()

            renderOption.content = text // content to encode
            renderOption.size = 800 // size of the final QR code image
            renderOption.borderWidth = 20 // width of the empty space around the QR code
            renderOption.ecl = ErrorCorrectionLevel.M
            renderOption.patternScale = 0.35f // (optional) specify a scale for patterns
            renderOption.roundedPatterns = true // (optional) if true, blocks will be drawn as dots instead
            renderOption.clearBorder = true // if set to true, the background will NOT be drawn on the border area
            val color = com.github.sumimakito.awesomeqr.option.color.Color()
            if (col.isNotEmpty())
            {
                previousColor = Color.parseColor("#$col")
                color.dark = previousColor!!
                renderOption.color = color // set a color palette for the QR code
            }
            else
            {
                if (previousColor != null)
                {
                    color.dark = previousColor!!
                    renderOption.color = color
                }
            }
            val background = StillBackground()
            if (bgImage.isNotEmpty())
            {
                previoudBackgroundImage = getBitmapFromURL(bgImage)
                background.bitmap = previoudBackgroundImage
                renderOption.background = background // set a background
            }
            else
            {
                if (previoudBackgroundImage != null)
                {
                    background.bitmap = previoudBackgroundImage
                    renderOption.background = background // set a background
                }
            }

            val logo = Logo()
            logo.bitmap = getDrawableToBitmap(context, R.drawable.whatsapp)
            renderOption.logo = logo // set a logo
            try {
                val result = AwesomeQrRenderer.render(renderOption)
                return if (result.bitmap != null) {
                    // play with the bitmap
                    result.bitmap!!
                } else {
                    // Oops, something gone wrong.
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Oops, something gone wrong.
                return null
            }
        }

        // THIS FUNCTION WILL CONVERT THE DRAWABLE IMAGE TO BITMAP
        private fun getDrawableToBitmap(context: Context, image: Int):Bitmap{

            return BitmapFactory.decodeResource(
                context.resources,
                image
            )
        }


        // THIS FUNCTION WILL DOWNLOAD IMAGE FROM URL AND CONVERT INTO BITMAP FOR BACKGROUND
        private fun getBitmapFromURL(src: String?): Bitmap? {
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
        }

    }

}