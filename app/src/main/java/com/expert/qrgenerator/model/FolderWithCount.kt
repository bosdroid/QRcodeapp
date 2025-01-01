package com.expert.qrgenerator.model

data class FolderWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val codeHistoryCount: Int
)
