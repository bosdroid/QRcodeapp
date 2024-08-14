package com.expert.qrgenerator.interfaces

interface LogoImagesCallback {
    fun onLogoImagesLoaded(images: List<String>)
    fun onLogoImagesError()
}