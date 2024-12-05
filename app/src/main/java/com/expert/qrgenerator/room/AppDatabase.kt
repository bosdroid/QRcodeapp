package com.expert.qrgenerator.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue

/**
 * The AppDatabase class serves as the main database holder for the Room database.
 * It provides the necessary methods to access the database DAOs.
 *
 * @property qrDao Provides access to the QRDao for data operations.
 */
@Database(entities = [CodeHistory::class, ListValue::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Abstract method to get the QRDao instance for interacting with the QR-related data.
    abstract fun qrDao(): QRDao

    companion object {
        // Migration from version 5 to version 6
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Step 1: Create a temporary table with the desired column types (TEXT for conversion, revenue, and expenses)
                database.execSQL("""
                    CREATE TABLE barcode_history_temp (
                        id INTEGER PRIMARY KEY NOT NULL,
                        login TEXT NOT NULL,
                        qrId TEXT NOT NULL,
                        data TEXT NOT NULL,
                        type TEXT NOT NULL,
                        userType TEXT NOT NULL,
                        codeType TEXT NOT NULL,
                        createdType TEXT NOT NULL,
                        localImagePath TEXT NOT NULL,
                        isDynamic TEXT NOT NULL,
                        generatedUrl TEXT,
                        createdAt TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        conversion TEXT,  -- Changing from FLOAT to TEXT
                        revenue TEXT,    -- Changing from FLOAT to TEXT
                        expenses TEXT    -- Changing from FLOAT to TEXT
                    )
                """)

                // Step 2: Copy data from the old table to the new table, converting FLOAT to TEXT
                database.execSQL("""
                    INSERT INTO barcode_history_temp (id, login, qrId, data, type, userType, codeType, createdType, 
                                                        localImagePath, isDynamic, generatedUrl, createdAt, notes, conversion, revenue, expenses)
                    SELECT id, login, qrId, data, type, userType, codeType, createdType, 
                           localImagePath, isDynamic, generatedUrl, createdAt, notes, 
                           CAST(conversion AS TEXT), CAST(revenue AS TEXT), CAST(expenses AS TEXT)
                    FROM barcode_history
                """)

                // Step 3: Drop the old table
                database.execSQL("DROP TABLE barcode_history")

                // Step 4: Rename the temporary table to the original table name
                database.execSQL("ALTER TABLE barcode_history_temp RENAME TO barcode_history")
            }
        }
    }
}
