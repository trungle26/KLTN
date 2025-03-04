package com.trungld.viberide.data.repository

import com.trungld.viberide.data.entity.Media
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getLocalMedia(): Flow<List<Media>>
    suspend fun fetchAndCacheMedia(): List<Media>
}