package com.expert.qrgenerator.room

import android.app.Application
import androidx.lifecycle.LiveData
import com.expert.qrgenerator.model.QREntity
import com.expert.qrgenerator.model.CodeHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseRepository(application: Application) {

    private var qrDao:QRDao
    private var scope = CoroutineScope(Dispatchers.IO)
    private var dynamicQrList: LiveData<List<QREntity>>
    private var allQRCodeHistory: LiveData<List<CodeHistory>>

    init {
        val appDatabase = AppDatabase.getInstance(application)
        qrDao = appDatabase.qrDao()
        dynamicQrList = qrDao.getAllDynamicQrCodes()
        allQRCodeHistory = qrDao.getAllQRCodeHistory()
    }


    fun insert(qrHistory: CodeHistory){
        scope.launch {
           qrDao.insert(qrHistory)
        }
    }

    fun update(inputUrl:String,url:String,id:Int){
        scope.launch {
            qrDao.update(inputUrl,url,id)
        }
    }

    fun delete(qrHistory: CodeHistory){
        scope.launch {
            qrDao.delete(qrHistory)
        }
    }
    fun getAllDynamicQrCodes():LiveData<List<QREntity>>{
        return dynamicQrList
    }

    fun getAllQRCodeHistory():LiveData<List<CodeHistory>>{
        return allQRCodeHistory
    }

}