package com.expert.qrgenerator.model

data class TableObject(
    var id: Int,
    var code_data: String,
    var date: String,
    var image:String
) {
    var dynamicColumns = mutableListOf<Pair<String, String>>()


    override fun toString(): String {
        return "$id:$code_data:$date:$image:$dynamicColumns"
    }
}