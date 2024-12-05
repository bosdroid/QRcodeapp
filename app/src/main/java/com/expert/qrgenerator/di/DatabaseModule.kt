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
object DatabaseModule {

    // Provides an instance of QRDao to the dependency graph
    @Provides
    fun provideQRDao(appDatabase: AppDatabase): QRDao = appDatabase.qrDao()

    // Provides a singleton instance of AppDatabase to the dependency graph
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "magic_qr_generator_database"
        )
            // Handle migrations destructively if the schema is updated
            .addMigrations(AppDatabase.MIGRATION_5_6)
            // Allow queries on the main thread (not recommended for production use)
            .allowMainThreadQueries()
            .build()

    // Provides an instance of DatabaseRepository to the dependency graph
    @Provides
    fun provideDatabaseRepository(qrDao: QRDao): DatabaseRepository = DatabaseRepository(qrDao)

}
