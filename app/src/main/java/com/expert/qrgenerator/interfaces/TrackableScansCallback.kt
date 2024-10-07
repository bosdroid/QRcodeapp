package com.expert.qrgenerator.interfaces

import com.expert.qrgenerator.model.TrackableScan

/**
 * Interface for callback methods related to TrackableScans loading.
 */
interface TrackableScansCallback {

    /**
     * Called when the TrackableScans have been successfully loaded.
     *
     * @param trackableScans A list of Trackable QR Code Scan History.
     */
    fun onTrackableScansLoaded(trackableScans: List<TrackableScan>)

    /**
     * Called when there is an error loading the TrackableScans History.
     */
    fun onTrackableScansError()
}
