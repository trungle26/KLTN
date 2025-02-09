package com.trungld.viberide.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trungld.viberide.ui.screens.home.HomeScreen
import com.trungld.viberide.ui.screens.LoginScreen
import com.trungld.viberide.ui.screens.SignUpScreen
import com.trungld.viberide.viewmodels.AudioViewModel
import com.trungld.viberide.viewmodels.AuthViewModel

@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    audioViewModel: AudioViewModel,
    startService: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(modifier, navController,authViewModel)
        }
        composable("signup") {
            SignUpScreen(modifier, navController,authViewModel)
        }
        composable("home") {
            HomeScreen(modifier, navController,authViewModel,audioViewModel, startService)
        }
    }
}