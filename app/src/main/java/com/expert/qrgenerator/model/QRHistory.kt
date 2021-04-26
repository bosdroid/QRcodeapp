package com.expert.qrgenerator.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "qr_codes_history")
data class QRHistory (
        @ColumnInfo(name = "login") val login:String,
        @ColumnInfo(name = "qrId") val qrId:String,
        @ColumnInfo(name = "data") val data:String,
        @ColumnInfo(name = "type") var type:String,
        @ColumnInfo(name = "userType") var userType:String,
        @ColumnInfo(name = "localImagePath") var localImagePath:String,
        @ColumnInfo(name = "isDynamic") var isDynamic:Int,
        @ColumnInfo(name = "generatedUrl") var generatedUrl:String = "",
        @ColumnInfo(name = "createdAt") var createdAt:Long,
        ): Serializable {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int=0
    override fun toString(): String {
        return "Login: $login\nQRID: $qrId\nData: $data\nType: $type\nUserType: $userType\nImagePath: $localImagePath\nIsDynamic: $isDynamic\nGeneratedUrl: $generatedUrl\nCreated At: $createdAt"
    }


}