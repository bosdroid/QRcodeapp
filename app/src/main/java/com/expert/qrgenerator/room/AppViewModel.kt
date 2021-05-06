package com.expert.qrgenerator.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.expert.qrgenerator.model.QREntity
import com.expert.qrgenerator.model.CodeHistory

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private var repository : DatabaseRepository = DatabaseRepository(application)
    private var dynamicQrCodes : LiveData<List<QREntity>>
    private var allQRCodeHistory: LiveData<List<CodeHistory>>

    init {
        dynamicQrCodes = repository.getAllDynamicQrCodes()
        allQRCodeHistory = repository.getAllQRCodeHistory()
    }

    public fun insert(qrHistory: CodeHistory){
        repository.insert(qrHistory)
    }

    public fun update(inputUrl:String,url:String,id:Int){
        repository.update(inputUrl,url,id)
    }

    public fun delete(qrHistory: CodeHistory){
        repository.delete(qrHistory)
    }

    public fun getAllDynamicQrCodes():LiveData<List<QREntity>>{
        return dynamicQrCodes
    }

    public fun getAllQRCodeHistory():LiveData<List<CodeHistory>>{
        return allQRCodeHistory
    }
}