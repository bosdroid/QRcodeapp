package com.expert.qrgenerator.room

import androidx.lifecycle.LiveData
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseRepository @Inject constructor(private val qrDao: QRDao) {

    private val dynamicQrList = mutableListOf<CodeHistory>()
    private val allQRCodeHistory = mutableListOf<CodeHistory>()
    private val allScanQRCodeHistory = mutableListOf<CodeHistory>()
    private val allCreateQRCodeHistory = mutableListOf<CodeHistory>()
    private val allListValues = mutableListOf<ListValue>()

    init {
        dynamicQrList.addAll(qrDao.getAllDynamicQrCodes())
        allQRCodeHistory.addAll(qrDao.getAllQRCodeHistory())
        allScanQRCodeHistory.addAll(qrDao.getAllScanQRCodeHistory())
        allCreateQRCodeHistory.addAll(qrDao.getAllCreateQRCodeHistory())
        allListValues.addAll(qrDao.getAllListValues())
    }

     fun insert(qrHistory: CodeHistory) {
        qrDao.insert(qrHistory)
    }

    suspend fun insertListValue(listValue: ListValue) = withContext(Dispatchers.IO) {
        qrDao.insertListValue(listValue)

    }

    fun update(inputUrl: String, url: String, id: Int) {

        qrDao.update(inputUrl, url, id)

    }

    fun updateHistory(qrHistory: CodeHistory) {

        qrDao.updateHistory(qrHistory)

    }

    fun getAllDynamicQrCodes(): List<CodeHistory> {
        return dynamicQrList
    }

    fun getAllQRCodeHistory(): List<CodeHistory> {
        return allQRCodeHistory
    }

    fun getAllScanQRCodeHistory(): List<CodeHistory> {
        return allScanQRCodeHistory
    }

    fun getAllCreateQRCodeHistory(): List<CodeHistory> {
        return allCreateQRCodeHistory
    }

    fun getAllListValues(): List<ListValue> {
        return allListValues
    }
}