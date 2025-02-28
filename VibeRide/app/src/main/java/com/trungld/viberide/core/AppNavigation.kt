package com.trungld.viberide.core

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.trungld.viberide.ui.screens.home.HomeScreen
import com.trungld.viberide.ui.screens.LoginScreen
import com.trungld.viberide.ui.screens.SignUpScreen
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

    NavHost(navController = navController, startDestination = "home") {
        composable("login") {
            LoginScreen(modifier, navController,authViewModel)
        }
        composable("signup") {
            SignUpScreen(modifier, navController,authViewModel)
        }
        composable("home") {
            HomeScreen(modifier, navController,authViewModel,audioViewModel,faceEmotionViewModel)
        }
    }
}