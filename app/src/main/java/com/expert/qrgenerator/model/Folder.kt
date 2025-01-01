package com.expert.qrgenerator.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String,
    var createdAt: Long = System.currentTimeMillis() // Timestamp for creation
    ){
    constructor() : this(0, "", 0L)
}
