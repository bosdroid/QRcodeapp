package com.expert.qrgenerator.room

import androidx.room.*
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue

@Dao
interface QRDao {

    /**
     * Inserts a new QR code history record into the database.
     * @param qrHistory The QR code history data to be inserted.
     */
    @Insert
    fun insert(qrHistory: CodeHistory):Long

    /**
     * GET a new QR code history record into the database.
     * @param qrHistory The QR code history data to be inserted.
     */
    @Query("SELECT * FROM barcode_history WHERE login=:loginId")
    fun getHistoryItem(loginId: String):CodeHistory?

    /**
     * Updates an existing QR code history record in the database.
     * @param inputUrl The new data for the QR code.
     * @param url The new generated URL for the QR code.
     * @param id The ID of the QR code record to be updated.
     */
    @Query("UPDATE barcode_history SET data = :inputUrl, generatedUrl = :url WHERE id = :id")
    fun update(inputUrl: String, url: String, id: Int)

    /**
     * Updates an existing QR code history record in the database.
     * @param qrHistory The QR code history data to be updated.
     */
    @Update
    fun updateHistory(qrHistory: CodeHistory)

    /**
     * Retrieves a list of dynamic QR code history records from the database.
     * @return A list of dynamic QR code history records.
     */
    @Query("SELECT * FROM barcode_history WHERE isDynamic = 1 ORDER BY qrId")
    fun getAllDynamicQrCodes(): List<CodeHistory>

    /**
     * Retrieves a list of all QR code history records from the database.
     * @return A list of all QR code history records.
     */
    @Query("SELECT * FROM barcode_history ORDER BY qrId")
    fun getAllQRCodeHistory(): List<CodeHistory>

    // THIS FUNCTION WILL GET ALL THE QR CODES HISTORY
    @Query("SELECT * FROM barcode_history ORDER BY qrId")
    fun getAllScanQRCodeHistory(): List<CodeHistory>

    // THIS FUNCTION WILL GET ALL THE QR CODES HISTORY
    @Query("SELECT * FROM barcode_history ORDER BY qrId")
    fun getAllCreateQRCodeHistory(): List<CodeHistory>

    // THIS FUNCTION WILL GET ALL THE QR CODES WITH TYPE TRACKABLE
    @Query("SELECT * FROM barcode_history WHERE type=:type OR type='advance'  ORDER BY qrId")
    fun getAllTrackableQRCodes(type:String): List<CodeHistory>

    /**
     * Inserts a new list value record into the database.
     * @param listValue The list value data to be inserted.
     */
    @Insert
    fun insertListValue(listValue: ListValue)

    /**
     * Retrieves a list of all list value records from the database, ordered by ID in descending order.
     * @return A list of all list value records.
     */
    @Query("SELECT * FROM list_values ORDER BY id DESC")
    fun getAllListValues(): List<ListValue>

}
