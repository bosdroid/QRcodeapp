package com.expert.qrgenerator.singleton

import com.google.api.services.sheets.v4.Sheets

object SheetService {

    // Singleton instance of Sheets, initially set to null
    @Volatile
    private var instance: Sheets? = null

    /**
     * Returns the singleton instance of Sheets.
     * Creates a new instance if it does not exist.
     *
     * @return The singleton Sheets instance.
     */
    fun getInstance(): Sheets {
        return instance ?: synchronized(this) {
            // Double-check if instance is still null before creating a new one
            instance ?: throw IllegalStateException("Google Sheets instance not initialized")
        }
    }

    /**
     * Initializes the singleton Sheets instance if it hasn't been initialized yet.
     *
     * @param sheet The Sheets instance to initialize.
     * @throws IllegalStateException if the instance is already initialized.
     */
    fun saveGoogleSheetInstance(sheet: Sheets) {
        if (instance == null) {
            synchronized(this) {
                if (instance == null) {
                    instance = sheet
                }
            }
        } else {
            throw IllegalStateException("Google Sheets instance already initialized")
        }
    }
}
