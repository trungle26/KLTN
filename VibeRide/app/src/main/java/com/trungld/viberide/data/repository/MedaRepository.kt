package com.trungld.viberide.data.repository

import com.trungld.viberide.data.entity.Media
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun getRecommendationsByEmotion(emotion: String, limit: Long = 20): List<Media>
    suspend fun cacheMedia(media: List<Media>)
    fun getCachedMedia(): Flow<List<Media>>
    suspend fun searchMediaFromFirestore(query: String): List<Media>
}