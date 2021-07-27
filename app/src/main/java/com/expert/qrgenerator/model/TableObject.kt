package com.expert.qrgenerator.model

import java.io.Serializable
import java.lang.StringBuilder

data class TableObject(
    var id: Int,
    var code_data: String,
    var date: String,
    var image:String
) :Serializable{
    var dynamicColumns = mutableListOf<Pair<String, String>>()


    override fun toString(): String {
        val stringBuilder = StringBuilder("ID: $id\nCODE_DATA: $code_data\nDate: $date\nImageLinks: $image\n")
        if (dynamicColumns.size > 0){
            for (i in 0 until dynamicColumns.size){
                stringBuilder.append("${dynamicColumns[i].first}: ${dynamicColumns[i].second}")
                if (i != dynamicColumns.size-1){
                    stringBuilder.append("\n")
                }
            }
        }
        return stringBuilder.toString()
    }

}