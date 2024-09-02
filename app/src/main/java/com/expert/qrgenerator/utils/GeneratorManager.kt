package com.expert.qrgenerator.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.ui.activities.DesignActivity
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.background.StillBackground
import com.github.sumimakito.awesomeqr.option.logo.Logo

class GeneratorManager {

    companion object{
        private var previousBackgroundImage: Bitmap? = null
        private var previousColor: Int? = null
        private var previousLogo: Bitmap? = null

        /**
         * Generates a QR code image with the specified options.
         *
         * @param context The context to be used for fetching images.
         * @param text The text to be encoded in the QR code.
         * @param col The color of the QR code. Should be a hex color code (e.g., "FF0000" for red).
         * @param bgImage URL of the background image for the QR code.
         * @param logoUrl URL of the logo to be placed in the center of the QR code.
         * @return A Bitmap object representing the generated QR code image, or null if an error occurred.
         */
        fun generatorQRImage(
            context: Context,
            text: String,
            col: String,
            bgImage: String,
            logoUrl: String
        ): Bitmap? {

            // Create RenderOption object to configure QR code rendering
            val renderOption = RenderOption().apply {
                this.content = text // Content to encode in QR code
                this.size = 800 // Size of the final QR code image
                this.borderWidth = 20 // Width of the border around the QR code
                this.patternScale = 0.45f // Scale for QR code patterns (optional)
                this.roundedPatterns = false // Use blocks instead of dots (optional)
                this.clearBorder = true // Clear the border area (optional)
            }

            // Set QR code color options
            val color = com.github.sumimakito.awesomeqr.option.color.Color().apply {
                background = 0xFFFFFFFF.toInt() // Default background color is white

                // If a color is provided, use it; otherwise, use previous color
                if (col.isNotEmpty()) {
                    this.dark = Color.parseColor("#$col")
                } else {
                    previousColor?.let {
                        this.dark = it
                    }
                }
            }
            renderOption.color = color

            // Set QR code background image
            val background = StillBackground().apply {
                if (bgImage.isNotEmpty()) {
                    previousBackgroundImage = ImageManager.getBitmapFromURL(context, bgImage)
                    this.bitmap = previousBackgroundImage
                } else {
                    previousBackgroundImage?.let {
                        this.bitmap = it
                    }
                }
            }
            renderOption.background = background

            // Set QR code logo
            val logo = Logo().apply {
                if (logoUrl.isNotEmpty()) {
                    previousLogo = ImageManager.getBitmapFromURL(context, logoUrl)
                    this.bitmap = previousLogo
                } else {
                    previousLogo?.let {
                        this.bitmap = it
                    }
                }
            }
            renderOption.logo = logo

            // Render the QR code and return the bitmap
            return try {
                val result = AwesomeQrRenderer.render(renderOption)
                result.bitmap // Return the generated bitmap or null if an error occurred
            } catch (e: Exception) {
                e.printStackTrace() // Log the error
                null // Return null if an error occurred
            }
        }


        /**
         * Resets the QR code generator by clearing previous settings.
         * This includes background image, color, and logo.
         */
        fun resetQRGenerator() {
            // Reset background image to null
            previousBackgroundImage = null

            // Reset color to null
            previousColor = null

            // Reset logo to null
            previousLogo = null
        }


        /**
         * Generates a QR code and launches the DesignActivity to display it.
         *
         * @param context The context from which the activity is started.
         * @param encodedData The data to be encoded into the QR code.
         * @param type The type of QR code being generated.
         */
        fun generateQRCode(context: Context, encodedData: String, type: String) {
            // Create a CodeHistory object to log QR code generation details
            val qrHistory = CodeHistory(
                "qrmagicapp",
                System.currentTimeMillis().toString(),
                encodedData,
                type,
                "free",
                "qr",
                "create",
                "",
                "0",
                "",
                System.currentTimeMillis().toString(),
                ""
            )

            // Create an intent to start the DesignActivity
            val intent = Intent(context, DesignActivity::class.java).apply {
                // Add encoded data and QR history to the intent extras
                putExtra("ENCODED_TEXT", encodedData)
                putExtra("QR_HISTORY", qrHistory)
            }

            // Start the DesignActivity with the intent
            context.startActivity(intent)
        }

    }
}