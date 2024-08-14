package com.expert.qrgenerator.interfaces

interface BackgroundImagesCallback {
    fun onBackgroundImagesLoaded(images: List<String>)
    fun onBackgroundImagesError()
}