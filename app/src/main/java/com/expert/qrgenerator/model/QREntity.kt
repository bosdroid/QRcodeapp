package com.expert.qrgenerator.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

//@Entity(tableName = "dynamic_qr_codes")
data class QREntity (
    @ColumnInfo(name = "login") val login:String,
    @ColumnInfo(name = "qrId") val qrId:String,
    @ColumnInfo(name = "userUrl") var userUrl:String,
    @ColumnInfo(name = "userType") var userType:String,
    @ColumnInfo(name = "generatedUrl") var generatedUrl:String = ""
        ):Serializable{
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int=0

}