package com.expert.qrgenerator.model

/**
 * Data class representing a type of QR code.
 *
 * @property image Resource ID for the QR code image.
 * @property name Name or description of the QR code type.
 * @property position The position or order of the QR code type.
 */

sealed class QRItem {
    data class Header(val title: String) : QRItem()
    data class QRType(val image: Int, val name: String, val position: Int) : QRItem()
}