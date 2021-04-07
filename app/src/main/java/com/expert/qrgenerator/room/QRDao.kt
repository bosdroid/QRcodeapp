package com.expert.qrgenerator.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.expert.qrgenerator.model.QREntity

@Dao
interface QRDao {

    // THIS FUNCTION WILL INSERT A NEW DYNAMIC QR CODE ENTITY DATA IN DATABASE
    @Insert
    fun insert(qrEntity: QREntity)

    // THIS FUNCTION WILL UPDATE THE EXISTING DYNAMIC QR CODE ENTITY DATA IN DATABASE
    @Query("UPDATE dynamic_qr_codes SET userUrl=:inputUrl,generatedUrl=:url WHERE id=:id")
    fun update(inputUrl:String,url:String,id:Int)

    // THIS FUNCTION WILL DELETE THE DYNAMIC QR CODE ENTITY DATA IN DATABASE
    @Delete
    fun delete(qrEntity: QREntity)

    // THIS FUNCTION WILL GET LIST OF DYNAMIC QR CODE ENTITY DATA FROM DATABASE
    @Query("SELECT * FROM dynamic_qr_codes ORDER BY qrId")
    fun getAllDynamicQrCodes():LiveData<List<QREntity>>

}