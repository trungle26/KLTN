package com.trungld.viberide.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.trungld.viberide.data.dao.MediaDao
import com.trungld.viberide.data.entity.Media
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val mediaDao: MediaDao,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : MediaRepository {

    override fun getLocalMedia(): Flow<List<Media>> {
        return mediaDao.getAllMedia()
    }

    override suspend fun fetchAndCacheMedia(): List<Media> {
        Log.d("MediaRepository", "Fetching from server...")
        val result = db.collection("media").get().await()
        val mediaItems = result.documents.mapNotNull { document ->
            val media = document.toObject(Media::class.java)
            media?.copy(id = document.id) // Set Firestore document ID
        }
        Log.d("MediaRepository", "Fetched ${mediaItems.size} items from Firestore")

        val updatedMediaList = mediaItems.map { media ->
            try {
                Log.d("MediaRepository", "Processing URL for: ${media.file_url}")
                val fileRef = storage.getReferenceFromUrl(media.file_url)
                val fileUrl = fileRef.downloadUrl.await().toString()
                media.copy(file_url = fileUrl)
            } catch (e: Exception) {
                Log.e("MediaRepository", "Failed to get URL for ${media.file_url}: ${e.message}", e)
                media
            }
        }
        mediaDao.deleteAllMedia()
        Log.d("MediaRepository", "Deleted old media from Room")
        mediaDao.insertMedia(updatedMediaList)
        Log.d("MediaRepository", "Inserted ${updatedMediaList.size} items into Room")

        val roomContents = mediaDao.getAllMedia().first()
        Log.d("MediaRepository", "Room now has ${roomContents.size} items after insert")

        return updatedMediaList
    }
}