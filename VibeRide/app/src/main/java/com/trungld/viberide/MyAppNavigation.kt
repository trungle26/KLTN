package com.trungld.viberide

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trungld.viberide.ui.screens.home.HomeScreen
import com.trungld.viberide.ui.screens.home.LoginScreen
import com.trungld.viberide.ui.screens.home.SignUpScreen
import com.trungld.viberide.viewmodels.AuthState
import com.trungld.viberide.viewmodels.AuthViewModel

@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
) {
    val navController = rememberNavController()
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("home")
            is AuthState.Unauthenticated -> navController.navigate("login")
            is AuthState.Error -> {
                val errorMessage = (authState.value as AuthState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()}
            else -> {}
        }
    }

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