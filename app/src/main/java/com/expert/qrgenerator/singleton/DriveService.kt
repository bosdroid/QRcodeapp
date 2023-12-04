package com.expert.qrgenerator.singleton

import com.google.api.services.drive.Drive

object DriveService {

    var instance:Drive?=null

    fun saveDriveInstance(drive: Drive){
        if (instance == null){
            instance = drive
        }
    }

}