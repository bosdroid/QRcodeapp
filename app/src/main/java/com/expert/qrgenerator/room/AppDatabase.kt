package com.expert.qrgenerator.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue

/**
 * The AppDatabase class serves as the main database holder for the Room database.
 * It provides the necessary methods to access the database DAOs.
 *
 * @property qrDao Provides access to the QRDao for data operations.
 */
@Database(entities = [CodeHistory::class, ListValue::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Abstract method to get the QRDao instance for interacting with the QR-related data.
     *
     * @return The QRDao instance.
     */
    abstract fun qrDao(): QRDao

}