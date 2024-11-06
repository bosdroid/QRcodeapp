package com.expert.qrgenerator.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "barcode_history")
data class CodeHistory(
    @ColumnInfo(name = "login") val login: String, // User login information
    @ColumnInfo(name = "qrId") var qrId: String, // Unique identifier for the QR code
    @ColumnInfo(name = "data") val data: String, // Data contained in the QR code
    @ColumnInfo(name = "type") var type: String, // Type of QR code (TEXT, LINK, CONTACT, etc.)
    @ColumnInfo(name = "userType") var userType: String, // User type (FREE, PREMIUM)
    @ColumnInfo(name = "codeType") var codeType: String, // Type of code (BARCODE or QR)
    @ColumnInfo(name = "createdType") var createdType: String, // Creation method (SCAN or CREATE)
    @ColumnInfo(name = "localImagePath") var localImagePath: String, // Path to local image
    @ColumnInfo(name = "isDynamic") var isDynamic: String, // Indicates if the QR code is dynamic (0 or 1)
    @ColumnInfo(name = "generatedUrl") var generatedUrl: String = "", // URL generated from the QR code
    @ColumnInfo(name = "createdAt") var createdAt: String, // Timestamp of creation
    @ColumnInfo(name = "notes") var notes: String // Additional notes
) : Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0 // Unique identifier for each entry

    @Ignore
    var conversion:Int = 0
    @Ignore var revenue:Int = 0
    @Ignore var expenses:Int= 0
    @Ignore var totalScans:Int = 0
    @Ignore var scanDateList:List<String> = emptyList()

    // Provides a string representation of the CodeHistory object
    override fun toString(): String {
        return "ID: $id\n" +
                "Login: $login\n" +
                "QRID: $qrId\n" +
                "Data: $data\n" +
                "Type: $type\n" +
                "UserType: $userType\n" +
                "CodeType: $codeType\n" +
                "CreatedType: $createdType\n" +
                "ImagePath: $localImagePath\n" +
                "IsDynamic: $isDynamic\n" +
                "GeneratedUrl: $generatedUrl\n" +
                "Created At: $createdAt\n" +
                "Notes: $notes"
    }
}