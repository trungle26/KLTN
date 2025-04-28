package com.trungld.viberide.data.repository

import com.trungld.viberide.domain.entity.Media
import com.trungld.viberide.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMediaRepository : MediaRepository {
    private val mediaCache = mutableListOf<Media>()
    private val favoritesMap = mutableMapOf<String, MutableList<String>>() // userId -> List<mediaId>
    private val mediaFlow = MutableStateFlow<List<Media>>(emptyList())

    // Simulated media database
    private val mediaDatabase = mutableListOf<Media>(Media("","","",listOf(),"","",""))

    // Control flags for simulating errors
    var shouldThrowErrorOnGetRecommendations = false
    var shouldThrowErrorOnSearch = false
    var shouldThrowErrorOnFavorites = false

    fun addMediaToDatabase(media: List<Media>) {
        mediaDatabase.addAll(media)
    }

    override suspend fun getRecommendationsByEmotion(emotion: String, limit: Long): List<Media> {
        if (shouldThrowErrorOnGetRecommendations) {
            throw Exception("Failed to fetch recommendations")
        }
        val genres = when (emotion) {
            "Happy" -> listOf("Upbeat", "Pop", "Dance")
            "Sad" -> listOf("Melancholy", "Blues", "Acoustic")
            "Angry" -> listOf("Rock", "Metal", "Punk")
            "Calm" -> listOf("Relax", "Ambient", "Chill")
            else -> listOf("Pop", "Indie")
        }
        return mediaDatabase
            .filter { media -> media.genre.any { it in genres } }
            .take(limit.toInt())
    }

    override suspend fun cacheMedia(media: List<Media>) {
        mediaCache.clear()
        mediaCache.addAll(media)
        mediaFlow.value = mediaCache.toList()
    }

    override fun getCachedMedia(): Flow<List<Media>> {
        return mediaFlow
    }

    override suspend fun searchMediaFromFirestore(query: String): List<Media> {
        if (shouldThrowErrorOnSearch) {
            throw Exception("Failed to search media")
        }
        return mediaDatabase
            .filter { media ->
                val q = query.lowercase()
                media.title.lowercase().contains(q) ||
                        media.artist.lowercase().contains(q) ||
                        media.genre.any { it.lowercase().contains(q) }
            }
            .take(10)
    }

    override suspend fun getFavorites(userId: String): List<Media> {
        if (shouldThrowErrorOnFavorites) {
            throw Exception("Failed to fetch favorites")
        }
        val favoriteMediaIds = favoritesMap[userId] ?: emptyList()
        return mediaDatabase.filter { it.id in favoriteMediaIds }
    }

    override suspend fun addToFavorites(userId: String, mediaId: String): Int {
        val userFavorites = favoritesMap.getOrPut(userId) { mutableListOf() }
        if (mediaId !in userFavorites) {
            userFavorites.add(mediaId)
        }
        return userFavorites.size
    }

    override suspend fun removeFromFavorites(userId: String, mediaId: String) {
        favoritesMap[userId]?.remove(mediaId)
    }
}