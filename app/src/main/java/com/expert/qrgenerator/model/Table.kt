package com.expert.qrgenerator.model

/**
 * Represents a table entity with its unique identifier and name.
 *
 * @property id The unique identifier for the table.
 * @property tableName The name of the table.
 */
data class Table(
        val id: Int,          // Unique identifier for the table
        val tableName: String // Name of the table
)
