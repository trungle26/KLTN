package com.trungld.viberide.core

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.trungld.viberide.presentation.screens.LoginScreen
import com.trungld.viberide.presentation.screens.SignUpScreen
import com.trungld.viberide.presentation.screens.home.HomeScreen
import com.trungld.viberide.presentation.screens.now_playing.NowPlayingScreen
import com.trungld.viberide.presentation.screens.search_input.SearchInputScreen
import com.trungld.viberide.presentation.screens.search_results.SearchResultsScreen
import com.trungld.viberide.presentation.viewmodels.AudioViewModel
import com.trungld.viberide.presentation.viewmodels.AuthViewModel
import com.trungld.viberide.presentation.viewmodels.FaceEmotionViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    audioViewModel: AudioViewModel,
    faceEmotionViewModel: FaceEmotionViewModel,
) {
    val navController = rememberAnimatedNavController()
    val searchHistory by authViewModel.recentSearches.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable(
            route = "login",
            enterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            LoginScreen(modifier, navController, authViewModel)
        }
        composable(
            route = "signup",
            enterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            SignUpScreen(modifier, navController, authViewModel)
        }
        composable(
            route = "home",
            enterTransition = { fadeIn() },
            popExitTransition = { fadeOut() }
        ) {
            HomeScreen(modifier, navController, authViewModel, audioViewModel, faceEmotionViewModel)
        }
        composable(
            route = "now_playing",
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            NowPlayingScreen(modifier, navController, audioViewModel)
        }
        composable("search_input") {
            SearchInputScreen(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                pastSearches = searchHistory,
                loadSearchHistory = { authViewModel.loadRecentSearches() },
                saveSearch = { query ->
                    authViewModel.saveSearch(query)
                },
                onSearch = { query ->
                    navController.navigate("search_results/$query") {
                        popUpTo("search_input") { inclusive = false } // Keep search_input in backstack
                    }
                }
            )
        }
        composable(
            route = "search_results/{query}",
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() }
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchResultsScreen(
                modifier = Modifier.fillMaxSize(),
                query = query,
                navController = navController,
                audioViewModel = audioViewModel,
            )
        }
    }
}