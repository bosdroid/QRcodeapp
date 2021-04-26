package com.expert.qrgenerator.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.expert.qrgenerator.model.QREntity
import com.expert.qrgenerator.model.QRHistory

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private var repository : DatabaseRepository = DatabaseRepository(application)
    private var dynamicQrCodes : LiveData<List<QREntity>>
    private var allQRCodeHistory: LiveData<List<QRHistory>>

    init {
        dynamicQrCodes = repository.getAllDynamicQrCodes()
        allQRCodeHistory = repository.getAllQRCodeHistory()
    }

    public fun insert(qrHistory: QRHistory){
        repository.insert(qrHistory)
    }

    public fun update(inputUrl:String,url:String,id:Int){
        repository.update(inputUrl,url,id)
    }

    public fun delete(qrHistory: QRHistory){
        repository.delete(qrHistory)
    }

    public fun getAllDynamicQrCodes():LiveData<List<QREntity>>{
        return dynamicQrCodes
    }

    public fun getAllQRCodeHistory():LiveData<List<QRHistory>>{
        return allQRCodeHistory
    }
}