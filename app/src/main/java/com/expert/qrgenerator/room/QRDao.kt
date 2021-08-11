package com.expert.qrgenerator.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue

@Dao
interface QRDao {

    // THIS FUNCTION WILL INSERT A NEW DYNAMIC QR CODE ENTITY DATA IN DATABASE
    @Insert
    fun insert(qrHistory: CodeHistory)

    // THIS FUNCTION WILL UPDATE THE EXISTING DYNAMIC QR CODE ENTITY DATA IN DATABASE
    @Query("UPDATE barcode_history SET data=:inputUrl,generatedUrl=:url WHERE id=:id")
    fun update(inputUrl:String,url:String,id:Int)

    // THIS FUNCTION WILL DELETE THE DYNAMIC QR CODE ENTITY DATA IN DATABASE
    @Update
    fun updateHistory(qrHistory: CodeHistory)

    // THIS FUNCTION WILL GET LIST OF DYNAMIC QR CODE ENTITY DATA FROM DATABASE
    @Query("SELECT * FROM barcode_history WHERE isDynamic=1 ORDER BY qrId")
    fun getAllDynamicQrCodes():LiveData<List<CodeHistory>>

    // THIS FUNCTION WILL GET ALL THE QR CODES HISTORY
    @Query("SELECT * FROM barcode_history ORDER BY qrId")
    fun getAllQRCodeHistory():LiveData<List<CodeHistory>>

    // THIS FUNCTION WILL GET ALL THE QR CODES HISTORY
    @Query("SELECT * FROM barcode_history ORDER BY qrId")
    fun getAllScanQRCodeHistory():LiveData<List<CodeHistory>>

    // THIS FUNCTION WILL GET ALL THE QR CODES HISTORY
    @Query("SELECT * FROM barcode_history ORDER BY qrId")
    fun getAllCreateQRCodeHistory():LiveData<List<CodeHistory>>

    @Insert
    fun insertListValue(listValue: ListValue)

    @Query("SELECT * FROM list_values ORDER BY id DESC")
    fun getAllListValues():LiveData<List<ListValue>>

}