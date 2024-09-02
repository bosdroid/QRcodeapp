package com.expert.qrgenerator.model

import java.io.Serializable
import java.lang.StringBuilder

/**
 * Represents an object with a set of dynamic columns and additional properties.
 *
 * @property id Unique identifier for the object.
 * @property code_data Some code associated with the object.
 * @property date Date related to the object.
 * @property image URL or path to an image associated with the object.
 * @property dynamicColumns A list of dynamic columns where each pair represents a column name and its value.
 * @property quantity The quantity associated with the object.
 */
data class TableObject(
    var id: Int,
    var code_data: String,
    var date: String,
    var image: String
) : Serializable {

    // List to hold dynamic columns as pairs of column names and their values
    var dynamicColumns = mutableListOf<Pair<String, String>>()

    // Quantity associated with the object
    var quantity: Int = 0

    /**
     * Provides a string representation of the TableObject.
     *
     * @return A formatted string with details of the TableObject.
     */
    override fun toString(): String {
        // Use StringBuilder for efficient string concatenation
        val stringBuilder = StringBuilder()

        // Append basic properties
        stringBuilder.append("ID: $id\n")
        stringBuilder.append("CODE_DATA: $code_data\n")
        stringBuilder.append("Date: $date\n")
        stringBuilder.append("ImageLinks: $image\n")

        // Append dynamic columns if they exist
        if (dynamicColumns.isNotEmpty()) {
            dynamicColumns.forEachIndexed { index, column ->
                stringBuilder.append("${column.first}: ${column.second}")
                // Append newline if it's not the last item
                if (index != dynamicColumns.size - 1) {
                    stringBuilder.append("\n")
                }
            }
        }

        return stringBuilder.toString()
    }
}
