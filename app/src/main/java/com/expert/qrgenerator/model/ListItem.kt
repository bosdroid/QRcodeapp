package com.expert.qrgenerator.model

import java.io.Serializable

/**
 * Represents an item in a list with an identifier and a value.
 *
 * @property id Unique identifier for the list item.
 * @property value The value or content of the list item.
 */
data class ListItem(
    val id: Int,          // Unique identifier for the list item.
    val value: String     // The value or content of the list item.
) : Serializable
