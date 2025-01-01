package com.expert.qrgenerator.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.Folder
import com.expert.qrgenerator.model.ListValue

/**
 * The AppDatabase class serves as the main database holder for the Room database.
 * It provides the necessary methods to access the database DAOs.
 *
 * @property qrDao Provides access to the QRDao for data operations.
 */
@Database(entities = [CodeHistory::class, ListValue::class, Folder::class], version = 9, exportSchema = false)
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
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Step 1: Add the new column 'tags' to the existing table
                database.execSQL("ALTER TABLE barcode_history ADD COLUMN tags TEXT NOT NULL DEFAULT 'null'")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new Folder table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `folders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `createdAt` INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                    )
                """)
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE barcode_history ADD COLUMN folder TEXT DEFAULT null")
            }
        }

    }
}
