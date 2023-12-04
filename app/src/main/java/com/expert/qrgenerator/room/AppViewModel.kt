package com.expert.qrgenerator.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AppViewModel @Inject constructor(private val repository: DatabaseRepository) : ViewModel() {

    private var dynamicQrCodes : LiveData<List<CodeHistory>> = repository.getAllDynamicQrCodes()
    private var allQRCodeHistory: LiveData<List<CodeHistory>> = repository.getAllQRCodeHistory()
    private var allScanQRCodeHistory: LiveData<List<CodeHistory>> = repository.getAllScanQRCodeHistory()
    private var allCreateQRCodeHistory: LiveData<List<CodeHistory>> = repository.getAllCreateQRCodeHistory()
    private var allListValues:LiveData<List<ListValue>> = repository.getAllListValues()

    fun insert(qrHistory: CodeHistory){
        viewModelScope.launch {
            repository.insert(qrHistory)
        }

    }

    fun insertListValue(listValue: ListValue){
        viewModelScope.launch {
            repository.insertListValue(listValue)
        }
    }

    fun update(inputUrl:String,url:String,id:Int){
        viewModelScope.launch {
            repository.update(inputUrl,url,id)
        }
    }

    fun updateHistory(qrHistory: CodeHistory){
        viewModelScope.launch {
            repository.updateHistory(qrHistory)
        }
    }

    fun getAllDynamicQrCodes():LiveData<List<CodeHistory>>{
        return dynamicQrCodes
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