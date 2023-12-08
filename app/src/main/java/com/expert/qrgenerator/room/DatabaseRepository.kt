package com.expert.qrgenerator.room

import androidx.lifecycle.LiveData
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseRepository @Inject constructor(private val qrDao: QRDao) {

    private var dynamicQrList: LiveData<List<CodeHistory>> = qrDao.getAllDynamicQrCodes()
    private var allQRCodeHistory: LiveData<List<CodeHistory>> = qrDao.getAllQRCodeHistory()
    private var allScanQRCodeHistory: LiveData<List<CodeHistory>> = qrDao.getAllScanQRCodeHistory()
    private var allCreateQRCodeHistory: LiveData<List<CodeHistory>> =
        qrDao.getAllCreateQRCodeHistory()
    private var allListValues: LiveData<List<ListValue>> = qrDao.getAllListValues()


    suspend fun insert(qrHistory: CodeHistory) = withContext(Dispatchers.IO) {
        qrDao.insert(qrHistory)
    }

    suspend fun insertListValue(listValue: ListValue) = withContext(Dispatchers.IO) {
        qrDao.insertListValue(listValue)

    }

    suspend fun update(inputUrl: String, url: String, id: Int) = withContext(Dispatchers.IO) {

        qrDao.update(inputUrl, url, id)

    }

    suspend fun updateHistory(qrHistory: CodeHistory) = withContext(Dispatchers.IO) {

        qrDao.updateHistory(qrHistory)

    }

    fun getAllDynamicQrCodes(): LiveData<List<CodeHistory>> {
        return dynamicQrList
    }

    fun getAllQRCodeHistory(): LiveData<List<CodeHistory>> {
        return allQRCodeHistory
    }

    fun getAllScanQRCodeHistory(): LiveData<List<CodeHistory>> {
        return allScanQRCodeHistory
    }

    fun getAllCreateQRCodeHistory(): LiveData<List<CodeHistory>> {
        return allCreateQRCodeHistory
    }

    fun getAllListValues(): LiveData<List<ListValue>> {
        return allListValues
    }
}