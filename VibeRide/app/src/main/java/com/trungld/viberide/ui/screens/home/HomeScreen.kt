package com.trungld.viberide.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.trungld.viberide.ui.screens.shared.cards.MediaControlCard
import com.trungld.viberide.ui.screens.shared.cards.MoodDetectionCard
import com.trungld.viberide.ui.screens.shared.components.ProfilePicture
import com.trungld.viberide.ui.screens.shared.cards.RecommendationCard
import com.trungld.viberide.ui.screens.shared.components.SearchBar
import com.trungld.viberide.ui.screens.shared.cards.YawnDetectionCard
import com.trungld.viberide.ui.theme.VibeRideTheme
import com.trungld.viberide.viewmodels.AuthState
import com.trungld.viberide.viewmodels.AuthViewModel
import com.trungld.viberide.viewmodels.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    var showOptions by remember { mutableStateOf(false) }
    val homeViewModel: HomeViewModel = viewModel()
    val mediaItems by homeViewModel.mediaItems.observeAsState(emptyList())

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> {}
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Home") }) }) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(it)
        ) {

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                // Top bar
                Row(
                    modifier = modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfilePicture(
                        imageUrl = null,
                        onClick = { showOptions = !showOptions }
                    )

                    if (showOptions) {
                        DropdownMenu(
                            expanded = true,
                            onDismissRequest = { showOptions = false }
                        ) {
                            if (authState.value == AuthState.Authenticated) {
                                // User is logged in: Show Log Out button
                                DropdownMenuItem(
                                    text = { Text("Log Out") },
                                    onClick = {
                                        try {
                                            authViewModel.signOut()
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "Error logging out: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            } else {
                                // User is not logged in: Show Log In and Sign Up buttons
                                DropdownMenuItem(
                                    text = { Text("Log In") },
                                    onClick = {
                                        showOptions = false
                                        navController.navigate("login")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sign Up") },
                                    onClick = {
                                        showOptions = false
                                        navController.navigate("signup")
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = modifier.width(8.dp))
                    SearchBar()

                }

                Spacer(modifier = modifier.height(16.dp))

                // Main content
                Row(
                    modifier = modifier
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RecommendationCard(modifier = Modifier.width(screenWidth / 3), mediaItems = mediaItems)
                    Spacer(modifier = modifier.width(16.dp))
                    MediaControlCard(modifier = Modifier.weight(1f))
                    Spacer(modifier = modifier.width(16.dp))

                    Column(modifier = modifier.weight(1f)) {
                        YawnDetectionCard()
                        Spacer(modifier = modifier.height(16.dp))
                        MoodDetectionCard()
                    }
                }
            }
        }

    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
private fun HomeScreenPreview() {
    VibeRideTheme {
        HomeScreen(navController = TODO(), authViewModel = TODO())
    }
}