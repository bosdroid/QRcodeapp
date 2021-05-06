package com.expert.qrgenerator.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.expert.qrgenerator.model.QREntity
import com.expert.qrgenerator.model.CodeHistory

@Database(entities = [QREntity::class,CodeHistory::class], version = 1,exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun qrDao() : QRDao

    companion object{
        private var instance:AppDatabase?=null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {

            if (instance == null)
            {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "magic_qr_generator_database"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
//                .addCallback(roomCallback)
                    .build()
            }

            return instance!!
        }
    }

}