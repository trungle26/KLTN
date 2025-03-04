package com.trungld.viberide.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.trungld.viberide.data.AppDatabase
import com.trungld.viberide.data.dao.MediaDao
import com.trungld.viberide.data.repository.MediaRepository
import com.trungld.viberide.data.repository.MediaRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideMediaDao(appDatabase: AppDatabase): MediaDao {
        return appDatabase.mediaDao()
    }

}