package com.expert.qrgenerator.room

import android.util.Log
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
        viewModelScope.launch {
            val dynamicQrCodes = repository.getAllDynamicQrCodes()
            val allQRCodeHistory = repository.getAllQRCodeHistory()
            val allScanQRCodeHistory = repository.getAllScanQRCodeHistory()
            val allCreateQRCodeHistory = repository.getAllCreateQRCodeHistory()
            val allListValues = repository.getAllListValues()

            // Log to check data
            Log.d("AppViewModel", "dynamicQrCodes: $dynamicQrCodes")
            Log.d("AppViewModel", "allQRCodeHistory: $allQRCodeHistory")
            Log.d("AppViewModel", "allScanQRCodeHistory: $allScanQRCodeHistory")
            Log.d("AppViewModel", "allCreateQRCodeHistory: $allCreateQRCodeHistory")
            Log.d("AppViewModel", "allListValues: $allListValues")

            // Update LiveData
            _dynamicQrCodes.postValue(dynamicQrCodes)
            _allQRCodeHistory.postValue(allQRCodeHistory)
            _allScanQRCodeHistory.postValue(allScanQRCodeHistory)
            _allCreateQRCodeHistory.postValue(allCreateQRCodeHistory)
            _allListValues.postValue(allListValues)
        }
    }

    // Function to insert a QR code history item
    fun insert(qrHistory: CodeHistory):Long {
//        viewModelScope.launch {
            return repository.insert(qrHistory)
//        }
    }

    // Function to GET a QR code history item
    fun getHistoryItem(qrHistory: CodeHistory):CodeHistory? {
        return repository.getHistoryItem(qrHistory)
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
