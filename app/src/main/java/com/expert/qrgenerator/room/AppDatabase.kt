package com.expert.qrgenerator.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.expert.qrgenerator.model.QREntity
import com.expert.qrgenerator.model.CodeHistory
import com.expert.qrgenerator.model.ListValue

@Database(entities = [CodeHistory::class, ListValue::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun qrDao(): QRDao

}