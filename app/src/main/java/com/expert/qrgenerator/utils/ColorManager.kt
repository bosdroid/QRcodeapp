package com.expert.qrgenerator.utils

import android.content.Context
import com.expert.qrgenerator.R

class ColorManager() {

    companion object{
        fun createColorList(context: Context):List<String>{
            val list = mutableListOf<String>()
            list.addAll(context.resources.getStringArray(R.array.color_values))
            return list
        }
    }



}