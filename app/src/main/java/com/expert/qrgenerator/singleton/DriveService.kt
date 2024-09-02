package com.expert.qrgenerator.singleton

import com.google.api.services.drive.Drive

object DriveService {

    // Singleton instance of Drive
    private var instance: Drive? = null

    /**
     * Saves the provided Drive instance if none exists.
     *
     * @param drive The Drive instance to be saved.
     * This method ensures that only one instance of Drive is stored.
     * If an instance already exists, the provided instance will not be saved.
     */
    fun saveDriveInstance(drive: Drive) {
        // Check if the instance is already initialized
        if (instance == null) {
            instance = drive
        }
    }

    /**
     * Retrieves the current Drive instance.
     *
     * @return The stored Drive instance, or null if none exists.
     */
    fun getDriveInstance(): Drive? {
        return instance
    }

    /**
     * Clears the stored Drive instance.
     * Use this method if you need to reset or remove the stored instance.
     */
    fun clearDriveInstance() {
        instance = null
    }
}
