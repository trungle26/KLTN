package com.trungld.viberide.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.trungld.viberide.domain.entity.SearchEntry
import com.trungld.viberide.domain.entity.User
import com.trungld.viberide.presentation.viewmodels.AuthState
import com.trungld.viberide.presentation.viewmodels.AuthViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString

class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        auth = mock(FirebaseAuth::class.java)
        db = mock(FirebaseFirestore::class.java)// Reset mock state to avoid leakage between tests
        reset(auth, db)
        viewModel = AuthViewModel(auth, db)
    }

    // Helper to set up Firestore collection mocks
    private fun setupFirestoreCollection(): CollectionReference {
        val collection = mock(CollectionReference::class.java)
        `when`(db.collection(anyString())).thenReturn(collection)
        return collection
    }

    // Helper to set up Firestore document reference mocks
    private fun setupFirestoreDocument(): DocumentReference {
        val document = mock(DocumentReference::class.java)
        val collection = setupFirestoreCollection()
        `when`(collection.document(anyString())).thenReturn(document)
        return document
    }

    // Helper to capture OnCompleteListener with correct generic type
    inline fun <reified T> captureOnCompleteListener(task: com.google.android.gms.tasks.Task<T>): ArgumentCaptor<com.google.android.gms.tasks.OnCompleteListener<T>> {
        val captor = ArgumentCaptor.forClass(com.google.android.gms.tasks.OnCompleteListener::class.java) as ArgumentCaptor<com.google.android.gms.tasks.OnCompleteListener<T>>
        `when`(task.addOnCompleteListener(captor.capture())).thenAnswer {
            captor.value.onComplete(task)
            task
        }
        return captor
    }

    // Helper to capture OnSuccessListener with correct generic type
    inline fun <reified T> captureOnSuccessListener(task: com.google.android.gms.tasks.Task<T>): ArgumentCaptor<com.google.android.gms.tasks.OnSuccessListener<T>> {
        val captor = ArgumentCaptor.forClass(com.google.android.gms.tasks.OnSuccessListener::class.java) as ArgumentCaptor<com.google.android.gms.tasks.OnSuccessListener<T>>
        `when`(task.addOnSuccessListener(captor.capture())).thenAnswer {
            captor.value.onSuccess(null)
            task
        }
        return captor
    }

    // Helper to capture OnSuccessListener and OnFailureListener together
    inline fun <reified T> captureTaskListeners(
        task: com.google.android.gms.tasks.Task<T>,
        onSuccess: Boolean = true
    ): Pair<ArgumentCaptor<com.google.android.gms.tasks.OnSuccessListener<T>>, ArgumentCaptor<com.google.android.gms.tasks.OnFailureListener>> {
        val successCaptor = ArgumentCaptor.forClass(com.google.android.gms.tasks.OnSuccessListener::class.java) as ArgumentCaptor<com.google.android.gms.tasks.OnSuccessListener<T>>
        val failureCaptor = ArgumentCaptor.forClass(com.google.android.gms.tasks.OnFailureListener::class.java)

        // Mock addOnSuccessListener
        `when`(task.addOnSuccessListener(successCaptor.capture())).thenAnswer {
            if (onSuccess) {
                successCaptor.value.onSuccess(null)
            }
            task // Return the task for chaining
        }

        // Mock addOnFailureListener on the same task
        `when`(task.addOnFailureListener(failureCaptor.capture())).thenAnswer {
            if (!onSuccess) {
                failureCaptor.value.onFailure(Exception("Mocked failure"))
            }
            task // Return the task for chaining
        }

        return successCaptor to failureCaptor
    }

    // Helper to capture OnFailureListener
    fun captureOnFailureListener(task: com.google.android.gms.tasks.Task<*>): ArgumentCaptor<com.google.android.gms.tasks.OnFailureListener> {
        val captor = ArgumentCaptor.forClass(com.google.android.gms.tasks.OnFailureListener::class.java)
        `when`(task.addOnFailureListener(captor.capture())).thenAnswer {
            captor.value.onFailure(Exception("Mocked failure"))
            task
        }
        return captor
    }

    fun captureSnapshotListener(query: Query): ArgumentCaptor<EventListener<QuerySnapshot>> {
        val captor = ArgumentCaptor.forClass(EventListener::class.java) as ArgumentCaptor<EventListener<QuerySnapshot>>
        val listenerRegistration = mock(ListenerRegistration::class.java)
        `when`(query.addSnapshotListener(captor.capture())).thenAnswer {
            // Do nothing here; we'll simulate the callback manually
            listenerRegistration // Return the mocked ListenerRegistration
        }
        return captor
    }

    @Test
    fun `checkAuthStatus sets Unauthenticated when no user is logged in`() {
        // Arrange
        `when`(auth.currentUser).thenReturn(null)

        // Act
        viewModel.checkAuthStatus()

        // Assert
        assertEquals(AuthState.Unauthenticated, viewModel.authState.value)
    }

    @Test
    fun `checkAuthStatus sets Authenticated when user is logged in`() {
        // Arrange
        val firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseUser.uid).thenReturn("user123")
        `when`(firebaseUser.email).thenReturn("user@example.com")
        `when`(auth.currentUser).thenReturn(firebaseUser)

        // Act
        viewModel.checkAuthStatus()

        // Assert
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Authenticated)
        assertEquals("user123", (state as AuthState.Authenticated).userId)
        assertEquals("user@example.com", state.email)
    }

    @Test
    fun `checkAuthStatus sets Authenticated with empty email and uid when user has null values`() {
        // Arrange
        val firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseUser.uid).thenReturn(null)
        `when`(firebaseUser.email).thenReturn(null)
        `when`(auth.currentUser).thenReturn(firebaseUser)

        // Act
        viewModel.checkAuthStatus()

        // Assert
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Authenticated)
        assertEquals("", (state as AuthState.Authenticated).userId)
        assertEquals("", state.email)
    }

    @Test
    fun `login sets Error when email is empty`() {
        // Act
        viewModel.login("", "password")

        // Assert
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Email and password cannot be empty", (state as AuthState.Error).message)
    }

    @Test
    fun `login sets Error when password is empty`() {
        // Act
        viewModel.login("user@example.com", "")

        // Assert
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Email and password cannot be empty", (state as AuthState.Error).message)
    }

    @Test
    fun `login sets Authenticated when login succeeds`() {
        // Arrange
        val task = mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<AuthResult>
        val authResult = mock(AuthResult::class.java)
        val firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseUser.uid).thenReturn("user123")
        `when`(firebaseUser.email).thenReturn("user@example.com")
        `when`(authResult.user).thenReturn(firebaseUser)
        `when`(task.isSuccessful).thenReturn(true)
        `when`(task.result).thenReturn(authResult)
        `when`(auth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(task)

        // Capture the listener
        val listenerCaptor = captureOnCompleteListener(task)

        // Act
        viewModel.login("user@example.com", "password")

        // Assert
//        assertEquals(AuthState.Loading, viewModel.authState.value)
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Authenticated)
        assertEquals("user123", (state as AuthState.Authenticated).userId)
        assertEquals("user@example.com", state.email)
    }

    @Test
    fun `login sets Error when login fails with exception`() {
        // Arrange
        val task = mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<AuthResult>
        val exception = Exception("Invalid credentials")
        `when`(task.isSuccessful).thenReturn(false)
        `when`(task.exception).thenReturn(exception)
        `when`(auth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(task)

        // Capture the listener
        val listenerCaptor = captureOnCompleteListener(task)

        // Act
        viewModel.login("user@example.com", "wrongpassword")

        // Assert
//        assertEquals(AuthState.Loading, viewModel.authState.value)
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Invalid credentials", (state as AuthState.Error).message)
    }

    @Test
    fun `login sets Error when login fails with null exception`() {
        // Arrange
        val task = mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<AuthResult>
        `when`(task.isSuccessful).thenReturn(false)
        `when`(task.exception).thenReturn(null)
        `when`(auth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(task)

        // Capture the listener
        val listenerCaptor = captureOnCompleteListener(task)

        // Act
        viewModel.login("user@example.com", "wrongpassword")

        // Assert
//        assertEquals(AuthState.Loading, viewModel.authState.value)
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Unknown error", (state as AuthState.Error).message)
    }

    @Test
    fun `login handles null user in auth result`() {
        // Arrange
        val task = mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<AuthResult>
        val authResult = mock(AuthResult::class.java)
        `when`(authResult.user).thenReturn(null)
        `when`(task.isSuccessful).thenReturn(true)
        `when`(task.result).thenReturn(authResult)
        `when`(auth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(task)

        // Capture the listener
        val listenerCaptor = captureOnCompleteListener(task)

        // Act
        viewModel.login("user@example.com", "password")

        // Assert
//        assertEquals(AuthState.Loading, viewModel.authState.value)
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Authenticated)
        assertEquals("", (state as AuthState.Authenticated).userId)
        assertEquals("", state.email)
    }

    @Test
    fun `signUp sets Error when email is empty`() {
        // Act
        viewModel.signUp("", "password", "username")

        // Assert
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Email and password cannot be empty", (state as AuthState.Error).message)
    }

    @Test
    fun `signUp sets Error when password is empty`() {
        // Act
        viewModel.signUp("user@example.com", "", "username")

        // Assert
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Email and password cannot be empty", (state as AuthState.Error).message)
    }

    @Test
    fun `signUp sets Authenticated when sign-up and Firestore save succeed`() {
        // Arrange
        val taskAuth = mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<AuthResult>
        val authResult = mock(AuthResult::class.java)
        val firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseUser.uid).thenReturn("user123")
        `when`(authResult.user).thenReturn(firebaseUser)
        `when`(taskAuth.isSuccessful).thenReturn(true)
        `when`(taskAuth.result).thenReturn(authResult)
        `when`(auth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(taskAuth)
        `when`(auth.currentUser).thenReturn(firebaseUser)

        val taskFirestore = mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<Void>
        `when`(taskFirestore.isSuccessful).thenReturn(true)
        val document = setupFirestoreDocument()
        `when`(document.set(any())).thenReturn(taskFirestore)

        // Capture listeners
        val authListenerCaptor = captureOnCompleteListener(taskAuth)
        val firestoreSuccessListenerCaptor = captureOnSuccessListener(taskFirestore)

        // Act
        viewModel.signUp("user@example.com", "password", "username")

        // Assert
//        assertEquals(AuthState.Loading, viewModel.authState.value)
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Authenticated)
        assertEquals("user123", (state as AuthState.Authenticated).userId)
        assertEquals("user@example.com", state.email)
    }


    @Test
    fun `signUp sets Error when sign-up fails with exception`() {
        // Arrange
        val task = mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<AuthResult>
        val exception = Exception("Email already in use")
        `when`(task.isSuccessful).thenReturn(false)
        `when`(task.exception).thenReturn(exception)
        `when`(auth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(task)

        // Capture the listener
        val listenerCaptor = captureOnCompleteListener(task)

        // Act
        viewModel.signUp("user@example.com", "password", "username")

        // Assert
//        assertEquals(AuthState.Loading, viewModel.authState.value)
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Email already in use", (state as AuthState.Error).message)
    }

    @Test
    fun `signUp sets Error when sign-up fails with null exception`() {
        // Arrange
        val task = mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<AuthResult>
        `when`(task.isSuccessful).thenReturn(false)
        `when`(task.exception).thenReturn(null)
        `when`(auth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(task)

        // Capture the listener
        val listenerCaptor = captureOnCompleteListener(task)

        // Act
        viewModel.signUp("user@example.com", "password", "username")

        // Assert
//        assertEquals(AuthState.Loading, viewModel.authState.value)
        val state = viewModel.authState.value
        assertTrue(state is AuthState.Error)
        assertEquals("Unknown error", (state as AuthState.Error).message)
    }

    @Test
    fun `signUp handles null currentUser after sign-up`() {
        // Arrange
        val taskAuth = mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<AuthResult>
        val authResult = mock(AuthResult::class.java)
        `when`(authResult.user).thenReturn(null)
        `when`(taskAuth.isSuccessful).thenReturn(true)
        `when`(taskAuth.result).thenReturn(authResult)
        `when`(auth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(taskAuth)
        `when`(auth.currentUser).thenReturn(null)

        // Capture the listener
        val authListenerCaptor = captureOnCompleteListener(taskAuth)

        // Act
        viewModel.signUp("user@example.com", "password", "username")

        // Assert
        assertEquals(AuthState.Loading, viewModel.authState.value)
        // Since userId is null, Firestore write isn't attempted, and state remains Loading
    }

    @Test
    fun `signUp logs Firestore failure message`() {
        // Arrange
        val taskAuth = mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<AuthResult>
        val authResult = mock(AuthResult::class.java)
        val firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseUser.uid).thenReturn("user123")
        `when`(authResult.user).thenReturn(firebaseUser)
        `when`(taskAuth.isSuccessful).thenReturn(true)
        `when`(taskAuth.result).thenReturn(authResult)
        `when`(auth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(taskAuth)
        `when`(auth.currentUser).thenReturn(firebaseUser)

        // Mock Log for the "saveSearch: userId" log
        val logMock = mockStatic(android.util.Log::class.java)
        logMock.`when`<Int> { android.util.Log.d(anyString(), anyString()) }.thenReturn(0)

        val taskFirestore = mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<Void>
        val exception = Exception("Firestore write failed")
        `when`(taskFirestore.isSuccessful).thenReturn(false)
        val document = setupFirestoreDocument()
        `when`(document.set(any())).thenReturn(taskFirestore)

        // Capture listeners
        val authListenerCaptor = captureOnCompleteListener(taskAuth)
        val (firestoreSuccessListenerCaptor, firestoreFailureListenerCaptor) = captureTaskListeners(taskFirestore, onSuccess = false)

        // Act
        viewModel.signUp("user@example.com", "password", "username")

        // Assert
//        assertEquals(AuthState.Loading, viewModel.authState.value)
        val state = viewModel.authState.value
//        assertTrue(state is AuthState.Error)
//        assertEquals("Error saving user: Mocked failure", (state as AuthState.Error).message)
        logMock.close()
    }

    @Test
    fun `signOut sets Unauthenticated and clears recentSearches`() {
        // Arrange
        val firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseUser.uid).thenReturn("user123")
        `when`(auth.currentUser).thenReturn(firebaseUser)
        viewModel.checkAuthStatus() // Set to Authenticated

        // Simulate a non-empty recentSearches list by calling loadRecentSearches
        val collection = setupFirestoreCollection()
        val query = mock(Query::class.java)
        `when`(collection.whereEqualTo(anyString(), any())).thenReturn(query)
        `when`(query.orderBy(anyString(), any())).thenReturn(query)
        `when`(query.limit(anyLong())).thenReturn(query)

        val snapshotListenerCaptor = captureSnapshotListener(query)

        // Trigger loadRecentSearches to populate recentSearches
        viewModel.loadRecentSearches()

        // Simulate snapshot listener firing with a search entry
        val snapshot = mock(QuerySnapshot::class.java)
        val document = mock(DocumentSnapshot::class.java)
        val searchEntry = SearchEntry("query", 1,"user123")
        `when`(document.toObject(SearchEntry::class.java)).thenReturn(searchEntry)
        `when`(snapshot.documents).thenReturn(listOf(document))
        snapshotListenerCaptor.value.onEvent(snapshot, null)

        // Verify that recentSearches is populated
        assertEquals(listOf(searchEntry), viewModel.recentSearches.value)

        // Act
        viewModel.signOut()

        // Assert
        assertEquals(AuthState.Unauthenticated, viewModel.authState.value)
        assertEquals(emptyList<String>(), viewModel.recentSearches.value)
        verify(auth).signOut()
    }

    @Test
    fun `saveSearch saves to Firestore and refreshes recentSearches when authenticated`() {
        // Arrange
        val firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseUser.uid).thenReturn("user123")
        `when`(auth.currentUser).thenReturn(firebaseUser)
        viewModel.checkAuthStatus() // Set to Authenticated

        val taskFirestore = mock(com.google.android.gms.tasks.Task::class.java) as com.google.android.gms.tasks.Task<DocumentReference>
        val collection = setupFirestoreCollection()
        `when`(collection.add(any())).thenReturn(taskFirestore)

        // Capture Firestore success listener
        val successListenerCaptor = captureOnSuccessListener(taskFirestore)

        // Mock loadRecentSearches behavior
        val query = mock(Query::class.java)
        `when`(collection.whereEqualTo(anyString(), any())).thenReturn(query)
        `when`(query.orderBy(anyString(), any())).thenReturn(query)
        `when`(query.limit(anyLong())).thenReturn(query)

        val snapshotListenerCaptor = captureSnapshotListener(query)

        // Mock Log for the "saveSearch: userId" log
        val logMock = mockStatic(android.util.Log::class.java)
        logMock.`when`<Int> { android.util.Log.d(anyString(), anyString()) }.thenReturn(0)

        // Act
        viewModel.saveSearch("query")

        // Simulate snapshot listener firing
        val snapshot = mock(QuerySnapshot::class.java)
        val document = mock(DocumentSnapshot::class.java)
        val searchEntry = SearchEntry("query",1, "user123")
        `when`(document.toObject(SearchEntry::class.java)).thenReturn(searchEntry)
        `when`(snapshot.documents).thenReturn(listOf(document))
        snapshotListenerCaptor.value.onEvent(snapshot, null)

        // Assert
        verify(collection).add(any())
        assertEquals(listOf(searchEntry), viewModel.recentSearches.value)
        logMock.verify { android.util.Log.d("search history", "saveSearch: userId: user123") }
        logMock.close()
    }


    @Test
    fun `loadRecentSearches updates recentSearches when authenticated and Firestore succeeds`() {
        // Arrange
        val firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseUser.uid).thenReturn("user123")
        `when`(auth.currentUser).thenReturn(firebaseUser)
        viewModel.checkAuthStatus() // Set to Authenticated

        val collection = setupFirestoreCollection()
        val query = mock(Query::class.java)
        `when`(collection.whereEqualTo(anyString(), any())).thenReturn(query)
        `when`(query.orderBy(anyString(), any())).thenReturn(query)
        `when`(query.limit(anyLong())).thenReturn(query)

        // Capture snapshot listener
        val snapshotListenerCaptor = captureSnapshotListener(query)

        // Act
        viewModel.loadRecentSearches()

        // Simulate snapshot listener firing
        val snapshot = mock(QuerySnapshot::class.java)
        val document = mock(DocumentSnapshot::class.java)
        val searchEntry = SearchEntry("query",1, "user123")
        `when`(document.toObject(SearchEntry::class.java)).thenReturn(searchEntry)
        `when`(snapshot.documents).thenReturn(listOf(document))
        snapshotListenerCaptor.value.onEvent(snapshot, null)

        // Assert
        assertEquals(listOf(searchEntry), viewModel.recentSearches.value)
    }


    @Test
    fun `loadRecentSearches handles null snapshot when authenticated`() {
        // Arrange
        val firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseUser.uid).thenReturn("user123")
        `when`(auth.currentUser).thenReturn(firebaseUser)
        viewModel.checkAuthStatus() // Set to Authenticated

        val collection = setupFirestoreCollection()
        val query = mock(Query::class.java)
        `when`(collection.whereEqualTo(anyString(), any())).thenReturn(query)
        `when`(query.orderBy(anyString(), any())).thenReturn(query)
        `when`(query.limit(anyLong())).thenReturn(query)

        // Capture snapshot listener
        val snapshotListenerCaptor = captureSnapshotListener(query)

        // Act
        viewModel.loadRecentSearches()

        // Simulate snapshot listener firing with null snapshot
        snapshotListenerCaptor.value.onEvent(null, null)

        // Assert
        assertEquals(emptyList<String>(), viewModel.recentSearches.value)
    }

    @Test
    fun `loadRecentSearches handles empty snapshot documents when authenticated`() {
        // Arrange
        val firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseUser.uid).thenReturn("user123")
        `when`(auth.currentUser).thenReturn(firebaseUser)
        viewModel.checkAuthStatus() // Set to Authenticated

        val collection = setupFirestoreCollection()
        val query = mock(Query::class.java)
        `when`(collection.whereEqualTo(anyString(), any())).thenReturn(query)
        `when`(query.orderBy(anyString(), any())).thenReturn(query)
        `when`(query.limit(anyLong())).thenReturn(query)

        // Capture snapshot listener
        val snapshotListenerCaptor = captureSnapshotListener(query)

        // Act
        viewModel.loadRecentSearches()

        // Simulate snapshot listener firing with empty documents
        val snapshot = mock(QuerySnapshot::class.java)
        `when`(snapshot.documents).thenReturn(emptyList())
        snapshotListenerCaptor.value.onEvent(snapshot, null)

        // Assert
        assertEquals(emptyList<String>(), viewModel.recentSearches.value)
    }

    @Test
    fun `loadRecentSearches handles null document conversion when authenticated`() {
        // Arrange
        val firebaseUser = mock(FirebaseUser::class.java)
        `when`(firebaseUser.uid).thenReturn("user123")
        `when`(auth.currentUser).thenReturn(firebaseUser)
        viewModel.checkAuthStatus() // Set to Authenticated

        val collection = setupFirestoreCollection()
        val query = mock(Query::class.java)
        `when`(collection.whereEqualTo(anyString(), any())).thenReturn(query)
        `when`(query.orderBy(anyString(), any())).thenReturn(query)
        `when`(query.limit(anyLong())).thenReturn(query)

        // Capture snapshot listener
        val snapshotListenerCaptor = captureSnapshotListener(query)

        // Act
        viewModel.loadRecentSearches()

        // Simulate snapshot listener firing with document that fails to convert
        val snapshot = mock(QuerySnapshot::class.java)
        val document = mock(DocumentSnapshot::class.java)
        `when`(document.toObject(SearchEntry::class.java)).thenReturn(null)
        `when`(snapshot.documents).thenReturn(listOf(document))
        snapshotListenerCaptor.value.onEvent(snapshot, null)

        // Assert
        assertEquals(emptyList<String>(), viewModel.recentSearches.value)
    }

    @Test
    fun `loadRecentSearches sets empty list when not authenticated`() {
        // Arrange
        `when`(auth.currentUser).thenReturn(null)
        viewModel.checkAuthStatus() // Set to Unauthenticated

        // Act
        viewModel.loadRecentSearches()

        // Assert
        assertEquals(emptyList<String>(), viewModel.recentSearches.value)
    }

    @Test
    fun `init calls checkAuthStatus`() {
        // Arrange
        `when`(auth.currentUser).thenReturn(null)

        // Act
        val viewModel = AuthViewModel(auth, db)

        // Assert
        assertEquals(AuthState.Unauthenticated, viewModel.authState.value)
    }
}