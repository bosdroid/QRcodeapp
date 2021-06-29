package com.expert.qrgenerator.room

import android.app.Application
import androidx.lifecycle.LiveData
import com.expert.qrgenerator.model.QREntity
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseRepository(application: Application) {

    private var qrDao:QRDao
    private var scope = CoroutineScope(Dispatchers.IO)
    private var dynamicQrList: LiveData<List<CodeHistory>>
    private var allQRCodeHistory: LiveData<List<CodeHistory>>
    private var allScanQRCodeHistory: LiveData<List<CodeHistory>>
    private var allCreateQRCodeHistory: LiveData<List<CodeHistory>>
    private var allListValues:LiveData<List<ListValue>>

    init {
        val appDatabase = AppDatabase.getInstance(application)
        qrDao = appDatabase.qrDao()
        dynamicQrList = qrDao.getAllDynamicQrCodes()
        allQRCodeHistory = qrDao.getAllQRCodeHistory()
        allScanQRCodeHistory = qrDao.getAllScanQRCodeHistory()
        allCreateQRCodeHistory = qrDao.getAllCreateQRCodeHistory()
        allListValues = qrDao.getAllListValues()
    }


    fun insert(qrHistory: CodeHistory){
        scope.launch {
           qrDao.insert(qrHistory)
        }
    }

    fun insertListValue(listValue: ListValue){
        scope.launch {
            qrDao.insertListValue(listValue)
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
    fun getAllDynamicQrCodes():LiveData<List<CodeHistory>>{
        return dynamicQrList
    }

    fun getAllQRCodeHistory():LiveData<List<CodeHistory>>{
        return allQRCodeHistory
    }

    fun getAllScanQRCodeHistory():LiveData<List<CodeHistory>>{
        return allScanQRCodeHistory
    }

    fun getAllCreateQRCodeHistory():LiveData<List<CodeHistory>>{
        return allCreateQRCodeHistory
    }

    fun getAllListValues():LiveData<List<ListValue>>{
        return allListValues
    }
}