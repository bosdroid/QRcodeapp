package com.expert.qrgenerator.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.Folder
import com.expert.qrgenerator.model.FolderWithCount
import com.expert.qrgenerator.model.ListValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    // LiveData to observe all create QR code history
    private val _allTrackableQRCodes = MutableLiveData<List<CodeHistory>>()
    val allTrackableQRCodes: LiveData<List<CodeHistory>> get() = _allTrackableQRCodes

    private val _allQRCodesTags = MutableLiveData<List<String>>()
    val allQRCodesTags: LiveData<List<String>> get() = _allQRCodesTags

    private val _allFolders = MutableLiveData<List<Folder>>()
    val allFolders: LiveData<List<Folder>> get() = _allFolders

    init {
        // Load data into LiveData upon initialization
        loadData()
    }

    // Function to load all data from the repository into LiveData
    private fun loadData() {
        viewModelScope.launch {
            // Update LiveData
            _dynamicQrCodes.postValue(repository.getAllDynamicQrCodes())
            _allQRCodeHistory.postValue(repository.getAllQRCodeHistory())
            _allScanQRCodeHistory.postValue(repository.getAllScanQRCodeHistory())
            _allCreateQRCodeHistory.postValue(repository.getAllCreateQRCodeHistory())
            _allTrackableQRCodes.postValue(repository.getAllTrackableQRCodes("trackable"))
            _allListValues.postValue(repository.getAllListValues())
            _allQRCodesTags.postValue(repository.getAllTags())
//            _allFolders.postValue(repository.getAllFolders())
        }
    }

    fun loadUpdateData() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = repository.getAllCreateQRCodeHistory()
            _allCreateQRCodeHistory.postValue(data)
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

    fun allCreateQRCodeHistory(folder:String?):LiveData<List<CodeHistory>>{
        return repository.getAllCreateQRCodeHistory(folder)
    }

    fun allFolders():LiveData<List<FolderWithCount>>{
        return repository.getAllFolders()
    }

    fun insertFolder(folder: Folder) {
        viewModelScope.launch {
            repository.insertFolder(folder)
        }
    }

    // Function to update a folder
    fun updateFolder(folder: Folder) {
        viewModelScope.launch {
            repository.updateFolder(folder)
        }
    }

    // Function to delete a folder
    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            repository.deleteFolder(folder)
        }
    }

}
