package com.expert.qrgenerator.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AppViewModel @Inject constructor(private val repository: DatabaseRepository) : ViewModel() {

    // LiveData to observe dynamic QR codes
    private val _dynamicQrCodes = MutableLiveData<List<CodeHistory>>()
    val dynamicQrCodes: LiveData<List<CodeHistory>> get() = _dynamicQrCodes

    // LiveData to observe all QR code history
    private val _allQRCodeHistory = MutableLiveData<List<CodeHistory>>()
    val allQRCodeHistory: LiveData<List<CodeHistory>> get() = _allQRCodeHistory

    // LiveData to observe all scan QR code history
    private val _allScanQRCodeHistory = MutableLiveData<List<CodeHistory>>()
    val allScanQRCodeHistory: LiveData<List<CodeHistory>> get() = _allScanQRCodeHistory

    // LiveData to observe all create QR code history
    private val _allCreateQRCodeHistory = MutableLiveData<List<CodeHistory>>()
    val allCreateQRCodeHistory: LiveData<List<CodeHistory>> get() = _allCreateQRCodeHistory

    // LiveData to observe all list values
    private val _allListValues = MutableLiveData<List<ListValue>>()
    val allListValues: LiveData<List<ListValue>> get() = _allListValues

    init {
        // Load data into LiveData upon initialization
        loadData()
    }

    // Function to load all data from the repository into LiveData
    private fun loadData() {
        _dynamicQrCodes.postValue(repository.getAllDynamicQrCodes())
        _allQRCodeHistory.postValue(repository.getAllQRCodeHistory())
        _allScanQRCodeHistory.postValue(repository.getAllScanQRCodeHistory())
        _allCreateQRCodeHistory.postValue(repository.getAllCreateQRCodeHistory())
        _allListValues.postValue(repository.getAllListValues())
    }

    // Function to insert a QR code history item
    fun insert(qrHistory: CodeHistory) {
        viewModelScope.launch {
            repository.insert(qrHistory)
        }
    }

    // Suspend function to insert a list value
    suspend fun insertListValue(listValue: ListValue) {
        repository.insertListValue(listValue)
    }

    // Function to update a QR code URL by ID
    fun update(inputUrl: String, url: String, id: Int) {
        viewModelScope.launch {
            repository.update(inputUrl, url, id)
        }
    }

    // Function to update a QR code history item
    fun updateHistory(qrHistory: CodeHistory) {
        viewModelScope.launch {
            repository.updateHistory(qrHistory)
        }
    }
}
