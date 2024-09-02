package com.expert.qrgenerator.interfaces

/**
 * Interface for callback methods related to background image loading.
 */
interface BackgroundImagesCallback {

    /**
     * Called when the background images have been successfully loaded.
     *
     * @param images A list of URLs or paths to the background images.
     */
    fun onBackgroundImagesLoaded(images: List<String>)

    /**
     * Called when there is an error loading the background images.
     */
    fun onBackgroundImagesError()
}
