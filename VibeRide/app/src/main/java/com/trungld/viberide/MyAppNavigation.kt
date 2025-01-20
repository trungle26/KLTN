package com.trungld.viberide

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trungld.viberide.ui.screens.home.HomeScreen
import com.trungld.viberide.ui.screens.home.LoginScreen
import com.trungld.viberide.ui.screens.home.SignUpScreen
import com.trungld.viberide.viewmodels.AuthViewModel

@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
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
            HomeScreen(modifier, navController,authViewModel)
        }
    }
}