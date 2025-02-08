package com.trungld.viberide.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.trungld.viberide.data.entities.Media

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _mediaItems = MutableLiveData<List<Media>>()
    val mediaItems: LiveData<List<Media>> = _mediaItems

    init {
        fetchMedia()
    }

    private fun fetchMedia() {
        db.collection("media")
            .get()
            .addOnSuccessListener { result ->
                val mediaList = result.documents.mapNotNull { document ->
                    document.toObject(Media::class.java)
                }
                _mediaItems.postValue(mediaList)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error fetching media: ${e.message}")
            }
    }
}
