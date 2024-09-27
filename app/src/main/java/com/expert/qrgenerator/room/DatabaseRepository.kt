package com.expert.qrgenerator.room

import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseRepository @Inject constructor(private val qrDao: QRDao) {

    // Cached lists of QR codes and list values
    private var dynamicQrList = listOf<CodeHistory>()
    private var allQRCodeHistory = listOf<CodeHistory>()
    private var allScanQRCodeHistory = listOf<CodeHistory>()
    private var allCreateQRCodeHistory = listOf<CodeHistory>()
    private var allListValues = listOf<ListValue>()

    init {
        // Initialize the cached lists with data from the DAO
        refreshData()
    }

    // Refresh cached data from the DAO
    private fun refreshData() {
        dynamicQrList = qrDao.getAllDynamicQrCodes()
        allQRCodeHistory = qrDao.getAllQRCodeHistory()
        allScanQRCodeHistory = qrDao.getAllScanQRCodeHistory()
        allCreateQRCodeHistory = qrDao.getAllCreateQRCodeHistory()
        allListValues = qrDao.getAllListValues()
    }

    // Insert a new QR code history record
    fun insert(qrHistory: CodeHistory) {
        qrDao.insert(qrHistory)
        refreshData() // Refresh data after insertion
    }

    // GET a new QR code history record
    fun getHistoryItem(qrHistory: CodeHistory): CodeHistory? {
        return qrDao.getHistoryItem(qrHistory.login)
    }

    // Insert a new list value record in a background thread
    suspend fun insertListValue(listValue: ListValue) = withContext(Dispatchers.IO) {
        qrDao.insertListValue(listValue)
        withContext(Dispatchers.Main) { refreshData() } // Refresh data after insertion
    }

    // Update a QR code record by ID
    fun update(inputUrl: String, url: String, id: Int) {
        qrDao.update(inputUrl, url, id)
        refreshData() // Refresh data after update
    }

    // Update a QR code history record
    fun updateHistory(qrHistory: CodeHistory) {
        qrDao.updateHistory(qrHistory)
        refreshData() // Refresh data after update
    }

    // Get all dynamic QR codes
    fun getAllDynamicQrCodes(): List<CodeHistory> {
        return dynamicQrList
    }

    // Get all QR code history records
    fun getAllQRCodeHistory(): List<CodeHistory> {
        return allQRCodeHistory
    }

    // Get all scan QR code history records
    fun getAllScanQRCodeHistory(): List<CodeHistory> {
        return allScanQRCodeHistory
    }

    // Get all create QR code history records
    fun getAllCreateQRCodeHistory(): List<CodeHistory> {
        return allCreateQRCodeHistory
    }

    // Get all list values
    fun getAllListValues(): List<ListValue> {
        return allListValues
    }
}
