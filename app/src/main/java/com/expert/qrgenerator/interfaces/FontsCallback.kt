package com.expert.qrgenerator.interfaces

import com.expert.qrgenerator.model.Fonts

interface FontsCallback {
    fun onFontsLoaded(fonts: List<Fonts>)
    fun onFontsError()
}