package com.expert.qrgenerator.model

/**
 * Data class representing a type of QR code.
 *
 * @property image Resource ID for the QR code image.
 * @property name Name or description of the QR code type.
 * @property position The position or order of the QR code type.
 */
data class QRTypes(
    val image: Int,       // Resource ID for the QR code image
    val name: String,     // Name or description of the QR code type
    val position: Int     // Position or order of the QR code type
)
