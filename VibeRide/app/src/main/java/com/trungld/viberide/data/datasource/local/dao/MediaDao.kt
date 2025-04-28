package com.trungld.viberide.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trungld.viberide.domain.entity.Media
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media")
    fun getAllMedia(): Flow<List<Media>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(media: List<Media>)

    @Query("DELETE FROM media")
    suspend fun deleteAllMedia()

    @Query("SELECT * FROM media WHERE id IN (:mediaIds)")
    suspend fun getMediaByIds(mediaIds: List<String>): List<Media>
}