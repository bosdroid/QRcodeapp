package com.expert.qrgenerator.room

import androidx.lifecycle.LiveData
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.Folder
import com.expert.qrgenerator.model.FolderWithCount
import com.expert.qrgenerator.model.ListValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseRepository @Inject constructor(private val qrDao: QRDao) {

    fun getAllTags():List<String>{
        return qrDao.getAllTags()
    }

    // GET a new QR code history record
    fun getHistoryItem(qrHistory: CodeHistory): CodeHistory? {
        return qrDao.getHistoryItem(qrHistory.login)
    }

    // Insert a new list value record in a background thread
    suspend fun insertListValue(listValue: ListValue) = withContext(Dispatchers.IO) {
        qrDao.insertListValue(listValue)
    }

    // Insert a new QR code history record
    fun insert(qrHistory: CodeHistory): Long {
        return qrDao.insert(qrHistory)
    }

    // Get all dynamic QR codes
    fun getAllDynamicQrCodes(): List<CodeHistory> {
        return qrDao.getAllDynamicQrCodes()
    }

    // Get all QR code history records
    fun getAllQRCodeHistory(): List<CodeHistory> {
        return qrDao.getAllQRCodeHistory()
    }

    // Get all scan QR code history records
    fun getAllScanQRCodeHistory(): List<CodeHistory> {
        return qrDao.getAllScanQRCodeHistory()
    }

    // Get all create QR code history records
    fun getAllCreateQRCodeHistory(): List<CodeHistory> {
        return qrDao.getAllCreateQRCodeHistory()
    }

    // Get all create QR code history records
    fun getAllCreateQRCodeHistory(folder: String?): LiveData<List<CodeHistory>> {
        return qrDao.getAllCreateQRCodeHistory(folder)
    }

//    fun getAllCreateQRCodeHistory(tag:String): LiveData<List<CodeHistory>> {
//        return qrDao.getAllScanQRCodeHistoryByTag(tag)
//    }

    // Get all list values
    fun getAllListValues(): List<ListValue> {
        return qrDao.getAllListValues()
    }

    // Update a QR code record by ID
    fun update(inputUrl: String, url: String, id: Int) {
        qrDao.update(inputUrl, url, id)
    }

    // Update a QR code history record
    fun updateHistory(qrHistory: CodeHistory) {
        qrDao.updateHistory(qrHistory)
    }

    // Get all trackable QR codes
    fun getAllTrackableQRCodes(type: String): List<CodeHistory> {
        return qrDao.getAllTrackableQRCodes(type)
    }

    // Insert a folder
     fun insertFolder(folder: Folder) {
        qrDao.insertFolder(folder)
    }

    // Get all folders
     fun getAllFolders(): LiveData<List<FolderWithCount>> {
        return qrDao.getAllFolders()
    }

    // Update a folder
     fun updateFolder(folder: Folder) {
        qrDao.updateFolder(folder)
    }

    // Delete a folder
     fun deleteFolder(folder: Folder) {
        qrDao.deleteFolder(folder)
    }
}
