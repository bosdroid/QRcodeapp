package com.expert.qrgenerator.di

import android.content.Context
import androidx.room.Room
import com.expert.qrgenerator.App
import com.expert.qrgenerator.repository.DataRepository
import com.expert.qrgenerator.room.AppDatabase
import com.expert.qrgenerator.room.DatabaseRepository
import com.expert.qrgenerator.room.QRDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    fun providesQRDao(appDatabase: AppDatabase): QRDao = appDatabase.qrDao()

    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "magic_qr_generator_database")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

    @Provides
    fun providesDatabaseRepository(qrDao: QRDao): DatabaseRepository = DatabaseRepository(qrDao)

}