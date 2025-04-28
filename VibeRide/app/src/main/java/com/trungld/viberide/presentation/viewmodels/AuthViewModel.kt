package com.trungld.viberide.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.trungld.viberide.domain.entity.SearchEntry
import com.trungld.viberide.domain.entity.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(
        value = AuthState.Unauthenticated
    )
    val authState: StateFlow<AuthState> = _authState

    // LiveData for recent searches
    private val _recentSearches = MutableStateFlow<List<SearchEntry>>(emptyList())
    val recentSearches: StateFlow<List<SearchEntry>> = _recentSearches

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated(auth.currentUser?.uid ?: "",auth.currentUser?.email?:"")
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated(task.result.user?.uid ?: "",task.result.user?.email?:"")
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
                }

            }
    }

    fun signUp(email: String, password: String, username: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    if (userId != null) {
                        val newUser = User(
                            id = userId,
                            username = username,
                            email = email
                        )

                        db.collection("users").document(userId)
                            .set(newUser)  // Firestore can convert the data class to JSON
                            .addOnSuccessListener {
                                _authState.value = AuthState.Authenticated(userId, email)
                            }
                            .addOnFailureListener { e ->
                                _authState.value =
                                    AuthState.Error("Error saving user: ${e.message}")
                                Log.d("Firebase", e.message.toString())
                            }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
                }

            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        _recentSearches.value = emptyList() // Clear searches on sign out
    }

    // Save a search to Firestore
    fun saveSearch(query: String) {
        val userId = (authState.value as? AuthState.Authenticated)?.userId
        Log.d("search history", "saveSearch: userId: $userId")
        val searchEntry = SearchEntry(
            query = query,
            userId = userId.toString()
        )

        db.collection("search_history").add(searchEntry)
            .addOnSuccessListener {
                loadRecentSearches() // Refresh the list after saving
            }
            .addOnFailureListener { e ->
                Log.e("search history", "Error saving search")
            }
    }

    // Load recent searches from Firestore
    fun loadRecentSearches() {
        val userId = (authState.value as? AuthState.Authenticated)?.userId
        if (userId != null) {
            db.collection("search_history")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10) // Limit to 10 recent searches
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("search history", "Error fetching search history")
                        return@addSnapshotListener
                    }
                    val searches = snapshot?.documents?.mapNotNull { it.toObject(SearchEntry::class.java) } ?: emptyList()
                    _recentSearches.value = (searches)
                }
        } else {
            _recentSearches.value = (emptyList()) // No user, no searches
        }
    }

}

sealed class AuthState {
    data class Authenticated(val userId: String, val email: String) : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}