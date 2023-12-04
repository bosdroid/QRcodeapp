package com.expert.qrgenerator.singleton

import com.google.api.services.sheets.v4.Sheets

object SheetService {

    var instance:Sheets?=null

    fun saveGoogleSheetInstance(sheet: Sheets){
        if (instance == null){
            instance = sheet
        }
    }

}