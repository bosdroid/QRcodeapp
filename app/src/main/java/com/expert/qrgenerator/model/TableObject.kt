package com.expert.qrgenerator.model

data class TableObject(
    var id: Int,
    var code_data: String,
    var title: String,
    var description: String,
    var brand: String,
    var country: String
) {
    var dynamicColumns = mutableListOf<Pair<String, String>>()
}