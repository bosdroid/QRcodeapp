package com.expert.qrgenerator.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AppViewModel @Inject constructor(private val repository: DatabaseRepository) : ViewModel() {

    private val _dynamicQrCodes = MutableLiveData<List<CodeHistory>>()
    val dynamicQrCodes: LiveData<List<CodeHistory>>
        get() = _dynamicQrCodes

    private val _allQRCodeHistory = MutableLiveData<List<CodeHistory>>()
    val allQRCodeHistory: LiveData<List<CodeHistory>>
        get() = _allQRCodeHistory

    private val _allScanQRCodeHistory = MutableLiveData<List<CodeHistory>>()
    val allScanQRCodeHistory: LiveData<List<CodeHistory>>
        get() = _allScanQRCodeHistory

    private val _allCreateQRCodeHistory = MutableLiveData<List<CodeHistory>>()
    val allCreateQRCodeHistory: LiveData<List<CodeHistory>>
        get() = _allCreateQRCodeHistory

    private val _allListValues = MutableLiveData<List<ListValue>>()
    val allListValues: LiveData<List<ListValue>>
        get() = _allListValues

    init {
        _dynamicQrCodes.postValue(repository.getAllDynamicQrCodes())
        _allQRCodeHistory.postValue(repository.getAllQRCodeHistory())
        _allScanQRCodeHistory.postValue(repository.getAllScanQRCodeHistory())
        _allCreateQRCodeHistory.postValue(repository.getAllCreateQRCodeHistory())
        _allListValues.postValue(repository.getAllListValues())
    }

     fun insert(qrHistory: CodeHistory){
            repository.insert(qrHistory)
    }

    suspend fun insertListValue(listValue: ListValue){
            repository.insertListValue(listValue)
    }

     fun update(inputUrl:String, url:String, id:Int){
            repository.update(inputUrl,url,id)
    }

     fun updateHistory(qrHistory: CodeHistory){
            repository.updateHistory(qrHistory)
    }
}