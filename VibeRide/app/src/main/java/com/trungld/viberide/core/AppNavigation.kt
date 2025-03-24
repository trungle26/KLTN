package com.trungld.viberide.core

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.trungld.viberide.ui.screens.LoginScreen
import com.trungld.viberide.ui.screens.SignUpScreen
import com.trungld.viberide.ui.screens.home.HomeScreen
import com.trungld.viberide.ui.screens.now_playing.NowPlayingScreen
import com.trungld.viberide.ui.screens.search_results.SearchResultsScreen
import com.trungld.viberide.viewmodels.AudioViewModel
import com.trungld.viberide.viewmodels.AuthViewModel
import com.trungld.viberide.viewmodels.FaceEmotionViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    audioViewModel: AudioViewModel,
    faceEmotionViewModel: FaceEmotionViewModel
) {
    val navController = rememberAnimatedNavController()

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
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            LoginScreen(modifier, navController, authViewModel)
        }
        composable(
            route = "signup",
            enterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            SignUpScreen(modifier, navController, authViewModel)
        }
        composable(
            route = "home",
            enterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            HomeScreen(modifier, navController, authViewModel, audioViewModel, faceEmotionViewModel)
        }
        composable(
            route = "now_playing",
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
        ) {
            NowPlayingScreen(modifier, navController, audioViewModel)
        }
        composable(
            route = "search_results/{query}",
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }) }
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