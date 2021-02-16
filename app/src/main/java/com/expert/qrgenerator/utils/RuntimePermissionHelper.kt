package com.expert.qrgenerator.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class RuntimePermissionHelper {

    //TODO We got permission checks in main activity duplicating?
    companion object{

        fun checkPermission(context: Context,permission:String):Boolean{

            return if (ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(context as Activity, arrayOf(permission),Constants.READ_STORAGE_REQUEST_CODE)
                false
            }
        }


    }

}