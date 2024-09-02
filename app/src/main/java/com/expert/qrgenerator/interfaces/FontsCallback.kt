package com.expert.qrgenerator.interfaces

import com.expert.qrgenerator.model.Fonts

/**
 * A callback interface for handling font loading operations.
 */
interface FontsCallback {

    /**
     * Called when fonts have been successfully loaded.
     *
     * @param fonts A list of loaded fonts.
     */
    fun onFontsLoaded(fonts: List<Fonts>)

    /**
     * Called when an error occurs during the font loading process.
     */
    fun onFontsError()
}
