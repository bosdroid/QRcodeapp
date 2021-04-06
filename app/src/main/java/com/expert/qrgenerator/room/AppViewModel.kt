package com.expert.qrgenerator.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.expert.qrgenerator.model.QREntity

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private var repository : DatabaseRepository = DatabaseRepository(application)
    private var dynamicQrCodes : LiveData<List<QREntity>>

    init {
        dynamicQrCodes = repository.getAllDynamicQrCodes()
    }

    public fun insert(qrEntity: QREntity){
        repository.insert(qrEntity)
    }

    public fun update(inputUrl:String,url:String,id:Int){
        repository.update(inputUrl,url,id)
    }

    public fun delete(qrEntity: QREntity){
        repository.delete(qrEntity)
    }

    public fun getAllDynamicQrCodes():LiveData<List<QREntity>>{
        return dynamicQrCodes
    }
}