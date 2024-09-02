package com.expert.qrgenerator.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class for the 'list_values' table in the Room database.
 *
 * @property id The primary key of the table. Auto-generated.
 * @property value A string value stored in the table.
 */
@Entity(tableName = "list_values")
data class ListValue(
    @ColumnInfo(name = "value") val value: String // Column to store the value.
) {
    @PrimaryKey(autoGenerate = true) // Primary key with auto-generated values.
    @ColumnInfo(name = "id") // Column to store the unique identifier for each entry.
    var id: Int = 0 // Default value is 0; will be auto-generated.
}
