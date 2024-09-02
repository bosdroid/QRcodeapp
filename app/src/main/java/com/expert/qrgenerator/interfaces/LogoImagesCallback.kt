package com.expert.qrgenerator.interfaces

/**
 * Interface to handle logo images loading events.
 */
interface LogoImagesCallback {

    /**
     * Called when the logo images have been successfully loaded.
     *
     * @param images A list of image URLs or paths.
     */
    fun onLogoImagesLoaded(images: List<String>)

    /**
     * Called when there is an error loading the logo images.
     */
    fun onLogoImagesError()
}
