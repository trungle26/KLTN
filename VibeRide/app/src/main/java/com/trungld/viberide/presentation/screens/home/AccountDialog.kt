package com.trungld.viberide.presentation.screens.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungld.viberide.presentation.theme.VibeRideTheme
import com.trungld.viberide.presentation.viewmodels.AuthState

@Composable
fun AccountDialog(
    authState: AuthState?,
    onDismiss: () -> Unit,
    onLogOut: () -> Unit,
    onLogIn: () -> Unit,
    onSignUp: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Account") },
        text = {
            when (authState) {
                is AuthState.Authenticated -> {
                    Text("Hi, ${authState.email}!")
                }
                is AuthState.Unauthenticated -> {
                    Text("You are not logged in.")
                }
                is AuthState.Loading -> {
                    Text("Loading...")
                }
                is AuthState.Error -> {
                    Text("Error: ${(authState as AuthState.Error).message}")
                }
                null -> {
                    Text("Checking status...")
                }
            }
        },
        confirmButton = {
            if (authState is AuthState.Authenticated) {
                OutlinedButton(onClick = onLogOut) {
                    Text("Log Out")
                }
            } else {
                Row() {
                    OutlinedButton (onClick = onLogIn) {
                        Text("Log In")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onSignUp) {
                        Text("Sign Up")
                    }
                }
            }
        },
        dismissButton = {
            FilledTonalButton (onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Preview AccountDialog
@Preview
@Composable
private fun AccountDialogPreview() {
    VibeRideTheme {
        AccountDialog(
            authState = AuthState.Unauthenticated,
            onDismiss = {},
            onLogOut = {},
            onLogIn = {},
            onSignUp = {},
        )
    }
    
}