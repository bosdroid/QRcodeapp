package com.expert.qrgenerator.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View


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
    }

}