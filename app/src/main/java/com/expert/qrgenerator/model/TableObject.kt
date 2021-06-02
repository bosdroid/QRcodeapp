package com.expert.qrgenerator.model

data class TableObject(
    var id: Int,
    var code_data: String,
    var date: String,
) {
    var dynamicColumns = mutableListOf<Pair<String, String>>()
}