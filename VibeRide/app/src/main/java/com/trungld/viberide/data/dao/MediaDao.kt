package com.trungld.viberide.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trungld.viberide.data.entity.Media
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media")
    fun getAllMedia(): Flow<List<Media>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: List<Media>)

    @Query("DELETE FROM media")
    suspend fun deleteAllMedia()
}