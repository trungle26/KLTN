package com.trungld.viberide.domain.repository

import com.trungld.viberide.domain.entity.Media
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun getRecommendationsByEmotion(emotion: String, limit: Long = 20): List<Media>
    suspend fun cacheMedia(media: List<Media>)
    fun getCachedMedia(): Flow<List<Media>>
    suspend fun searchMediaFromFirestore(query: String): List<Media>
    suspend fun getFavorites(userId: String): List<Media>
    suspend fun addToFavorites(userId: String, mediaId: String): Int
    suspend fun removeFromFavorites(userId: String, mediaId: String)
}