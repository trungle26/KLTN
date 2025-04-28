package com.trungld.viberide.data.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.trungld.viberide.data.datasource.local.dao.MediaDao
import com.trungld.viberide.domain.entity.Media

@Database(entities = [Media::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
}