package com.trungld.viberide.data.datasource.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.trungld.viberide.domain.entity.Media
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
) {

    private val emotionToGenreMap = mapOf(
        "Happy" to listOf("Upbeat", "Pop", "Dance"),
        "Sad" to listOf("Melancholy", "Blues", "Acoustic"),
        "Angry" to listOf("Rock", "Metal", "Punk"),
        "Calm" to listOf("Relax", "Ambient", "Chill"),
        "Neutral" to listOf("Pop", "Indie")
    )

    suspend fun getRecommendationsByEmotion(emotion: String, limit: Long): List<Media> {
        val genres = emotionToGenreMap[emotion] ?: emotionToGenreMap["Neutral"] ?: emptyList()

        // Fetch Firestore docs
        val querySnapshot = firestore.collection("media")
            .whereArrayContainsAny("genre", genres)
            .limit(limit)
            .get()
            .await() // Safe here in a suspend function

        val mediaItems = querySnapshot.documents.mapNotNull { document ->
            val media = document.toObject(Media::class.java)
            media?.copy(id = document.id) // Set Firestore document ID
        }
        Log.d("MediaRepository", "Fetched ${mediaItems.size} items from Firestore")

        // Fetch Storage URLs concurrently
        return withContext(Dispatchers.IO) {
            mediaItems.map { media ->
                async {
                    try {
                        Log.d("MediaRepository", "Processing URL for: ${media.file_url}")
                        val fileRef = storage.getReferenceFromUrl(media.file_url)
                        val fileUrl =
                            fileRef.downloadUrl.await().toString() // Safe in async coroutine
                        media.copy(file_url = fileUrl)
                    } catch (e: Exception) {
                        Log.e(
                            "MediaRepository",
                            "Failed to get URL for ${media.file_url}: ${e.message}",
                            e
                        )
                        media // Return original media if URL fetch fails
                    }
                }
            }.awaitAll() // Wait for all URLs to resolve
        }
    }

    suspend fun searchMediaFromFirestore(query: String): List<Media> {
        val query = query.trim().lowercase()
        if (query.isEmpty()) {
            Log.d("Search Media", "searchMediaFromFirestore: Query is empty")
            return emptyList()
        }
        // Use orderBy, startAt, and endAt for prefix search
        val querySnapshot = firestore.collection("media")
            .get()
            .await()

        val mediaItems = querySnapshot.documents.mapNotNull { document ->
            val media = document.toObject(Media::class.java)
            media?.copy(id = document.id)
        }.filter { media ->
            val matchesTitle = media.title.lowercase().contains(query)
            val matchesArtist = media.artist.lowercase().contains(query)
            val matchesGenre = media.genre.any { it.lowercase().contains(query) }
            matchesTitle || matchesArtist || matchesGenre
        }.take(10) // Limit to 10 results after filtering
        Log.d("Search Media", "searchMediaFromFirestore: found ${mediaItems.size} results")
        // Fetch Storage URLs concurrently
        return withContext(Dispatchers.IO) {
            mediaItems.map { media ->
                async {
                    try {
                        Log.d("MediaRepository", "Processing URL for: ${media.file_url}")
                        val fileRef = storage.getReferenceFromUrl(media.file_url)
                        val fileUrl =
                            fileRef.downloadUrl.await().toString() // Safe in async coroutine
                        media.copy(file_url = fileUrl)
                    } catch (e: Exception) {
                        Log.e(
                            "MediaRepository",
                            "Failed to get URL for ${media.file_url}: ${e.message}",
                            e
                        )
                        media // Return original media if URL fetch fails
                    }
                }
            }.awaitAll() // Wait for all URLs to resolve
        }
    }

    suspend fun getFavorites(userId: String): List<Media> = withContext(Dispatchers.IO) {
        try {
            val favoritesSnapshot = firestore.collection("favorites")
                .whereEqualTo("user_uid", userId)
                .get()
                .await()

            val favoriteMediaIds =
                favoritesSnapshot.documents.mapNotNull { it.getString("media_id") }
            if (favoriteMediaIds.isEmpty()) return@withContext emptyList()

            val mediaSnapshot = firestore.collection("media")
                .whereIn("id", favoriteMediaIds)
                .get()
                .await()
            val firestoreMedia = mediaSnapshot.toObjects(Media::class.java)

            return@withContext firestoreMedia.map { media ->
                async {
                    try {
                        Log.d("MediaRepository", "Processing URL for: ${media.file_url}")
                        val fileRef = storage.getReferenceFromUrl(media.file_url)
                        val fileUrl =
                            fileRef.downloadUrl.await().toString() // Safe in async coroutine
                        media.copy(file_url = fileUrl)
                    } catch (e: Exception) {
                        Log.e(
                            "MediaRepository",
                            "Failed to get URL for ${media.file_url}: ${e.message}",
                            e
                        )
                        media // Return original media if URL fetch fails
                    }
                }
            }.awaitAll() // Wait for all URLs to resolve
        } catch (e: Exception) {
            Log.e("Get Favorites", "Error fetching favorites for user $userId:${e.message}")
            emptyList() // Return empty list on error
        }
    }

    suspend fun addToFavorites(userId: String, mediaId: String) = withContext(Dispatchers.IO) {
        try {
            val favoriteData = hashMapOf(
                "user_uid" to userId,
                "media_id" to mediaId
            )
            firestore.collection("favorites")
                .add(favoriteData)
                .await()
            Log.d("Add to Favorites", "Added $mediaId to favorites for user $userId")
        } catch (e: Exception) {
            Log.d(
                "Add to Favorites",
                "Error adding $mediaId to favorites for user $userId: ${e.message}"
            )
        }
    }

    suspend fun removeFromFavorites(userId: String, mediaId: String) = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection("favorites")
                .whereEqualTo("user_uid", userId)
                .whereEqualTo("media_id", mediaId)
                .get()
                .await()
            querySnapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            throw e
        }
    }
}