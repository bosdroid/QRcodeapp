package com.expert.qrgenerator.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.expert.qrgenerator.model.QREntity

@Dao
interface QRDao {

    @Insert
    fun insert(qrEntity: QREntity)

    @Query("UPDATE dynamic_qr_codes SET userUrl=:inputUrl,generatedUrl=:url WHERE id=:id")
    fun update(inputUrl:String,url:String,id:Int)

    @Delete
    fun delete(qrEntity: QREntity)

    @Query("SELECT * FROM dynamic_qr_codes ORDER BY qrId")
    fun getAllDynamicQrCodes():LiveData<List<QREntity>>

}