package com.expert.qrgenerator.model

import java.io.Serializable

/**
 * Data class representing a Sheet with an ID and name.
 * Implements Serializable for easy object serialization.
 *
 * @property id Unique identifier for the Sheet.
 * @property name Name of the Sheet.
 */
data class Sheet(
    val id: String,  // Unique identifier for the Sheet
    val name: String // Name of the Sheet
) : Serializable { // Implements Serializable for object serialization

    /**
     * Returns a string representation of the Sheet.
     * Overrides the default toString() method to return the name property.
     *
     * @return The name of the Sheet.
     */
    override fun toString(): String {
        return name
    }
}
