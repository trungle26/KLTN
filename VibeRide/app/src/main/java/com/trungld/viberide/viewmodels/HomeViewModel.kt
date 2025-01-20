package com.trungld.viberide.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.trungld.viberide.data.entities.Media
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // StateFlow to hold the list of media items
    private val _mediaList = MutableStateFlow<List<Media>>(emptyList())
    val mediaList: StateFlow<List<Media>> = _mediaList

    // StateFlow to hold any potential errors
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchMedia()
    }

    private fun fetchMedia() {
        db.collection("media").get()
            .addOnSuccessListener { result ->
                val mediaItems = result.documents.mapNotNull { document ->
                    document.toObject(Media::class.java)
                }
                _mediaList.value = mediaItems
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = exception.message
            }

    }
}
