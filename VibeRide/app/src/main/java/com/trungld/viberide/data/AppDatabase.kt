package com.trungld.viberide.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.trungld.viberide.data.dao.MediaDao
import com.trungld.viberide.data.entity.Media

@Database(entities = [Media::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
}