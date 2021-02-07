package com.expert.qrgenerator.utils

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import com.vipul.hp_hp.library.Layout_to_Image





class ImageManager {

    companion object{
        // LAYOUT TO IMAGE IS A LIBRARY OBJECT THAT WILL CONVERT THE SPECIFIC LAYOUT TO IMAGE
        var layout_to_image:Layout_to_Image? = null

        // THIS FUNCTION WILL CONVERT BITMAP IMAGE FROM VIEW
        fun loadBitmapFromView(context: Context,view: View): Bitmap? {
           layout_to_image = Layout_to_Image(context,view)
           return layout_to_image!!.convert_layout()
        }
    }

}