package com.expert.qrgenerator.room

import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseRepository @Inject constructor(private val qrDao: QRDao) {

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
}
