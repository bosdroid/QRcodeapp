package com.expert.qrgenerator.room

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.expert.qrgenerator.model.QREntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseRepository(application: Application) {

    private var qrDao:QRDao
    private var scope = CoroutineScope(Dispatchers.IO)
    private var dynamicQrList: LiveData<List<QREntity>>

    init {
        val appDatabase = AppDatabase.getInstance(application)
        qrDao = appDatabase.qrDao()
        dynamicQrList = qrDao.getAllDynamicQrCodes()
    }


    fun insert(qrEntity: QREntity){
        scope.launch {
           qrDao.insert(qrEntity)
        }
    }

    fun update(inputUrl:String,url:String,id:Int){
        scope.launch {
            qrDao.update(inputUrl,url,id)
        }
    }

    fun delete(qrEntity: QREntity){
        scope.launch {
            qrDao.delete(qrEntity)
        }
    }
    fun getAllDynamicQrCodes():LiveData<List<QREntity>>{
        return dynamicQrList
    }

}