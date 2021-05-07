package com.expert.qrgenerator.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "codes_history")
data class CodeHistory (
        @ColumnInfo(name = "login") val login:String,
        @ColumnInfo(name = "qrId") val qrId:String,
        @ColumnInfo(name = "data") val data:String,
        @ColumnInfo(name = "type") var type:String, // QR CODE TYPE -> TEXT,LINK,CONTACT, ETC
        @ColumnInfo(name = "userType") var userType:String, // FREE,PREMIUM
        @ColumnInfo(name = "codeType") var codeType:String, // BARCODE OR QR
        @ColumnInfo(name = "createdType") var createdType:String, // SCAN OR CREATE
        @ColumnInfo(name = "localImagePath") var localImagePath:String,
        @ColumnInfo(name = "isDynamic") var isDynamic:Int, // 0 OR 1
        @ColumnInfo(name = "generatedUrl") var generatedUrl:String = "",
        @ColumnInfo(name = "createdAt") var createdAt:Long,
        ): Serializable {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int=0
    override fun toString(): String {
        return "ID: $id\nLogin: $login\nQRID: $qrId\nData: $data\nType: $type\nUserType: $userType\nCodeType: $codeType\n" +
                "CreatedType: $createdType \nImagePath: $localImagePath\nIsDynamic: $isDynamic\nGeneratedUrl: $generatedUrl\nCreated At: $createdAt"
    }


}