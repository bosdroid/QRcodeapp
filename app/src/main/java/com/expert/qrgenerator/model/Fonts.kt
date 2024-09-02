package com.expert.qrgenerator.model

/**
 * Represents a font with its associated image and file.
 *
 * @property fontImage The URL or path to the image representing the font.
 * @property fontFile The URL or path to the font file.
 */
data class Fonts(
    val fontImage: String, // URL or path to the font image
    val fontFile: String   // URL or path to the font file
)
