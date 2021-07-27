package com.expert.qrgenerator.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.expert.qrgenerator.model.QREntity
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private var repository : DatabaseRepository = DatabaseRepository(application)
    private var dynamicQrCodes : LiveData<List<CodeHistory>>
    private var allQRCodeHistory: LiveData<List<CodeHistory>>
    private var allScanQRCodeHistory: LiveData<List<CodeHistory>>
    private var allCreateQRCodeHistory: LiveData<List<CodeHistory>>
    private var allListValues:LiveData<List<ListValue>>

    init {
        dynamicQrCodes = repository.getAllDynamicQrCodes()
        allQRCodeHistory = repository.getAllQRCodeHistory()
        allScanQRCodeHistory = repository.getAllScanQRCodeHistory()
        allCreateQRCodeHistory = repository.getAllCreateQRCodeHistory()
        allListValues = repository.getAllListValues()
    }

    public fun insert(qrHistory: CodeHistory){
        repository.insert(qrHistory)
    }

    public fun insertListValue(listValue: ListValue){
        repository.insertListValue(listValue)
    }

    public fun update(inputUrl:String,url:String,id:Int){
        repository.update(inputUrl,url,id)
    }

    public fun updateHistory(qrHistory: CodeHistory){
        repository.updateHistory(qrHistory)
    }

    public fun getAllDynamicQrCodes():LiveData<List<CodeHistory>>{
        return dynamicQrCodes
    }

    public fun getAllQRCodeHistory():LiveData<List<CodeHistory>>{
        return allQRCodeHistory
    }

    public fun getAllScanQRCodeHistory():LiveData<List<CodeHistory>>{
        return allScanQRCodeHistory
    }

    public fun getAllCreateQRCodeHistory():LiveData<List<CodeHistory>>{
        return allCreateQRCodeHistory
    }
    public fun getAllListValues():LiveData<List<ListValue>>{
        return allListValues
    }
}