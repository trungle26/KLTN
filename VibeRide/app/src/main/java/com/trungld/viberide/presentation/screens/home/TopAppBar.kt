package com.trungld.viberide.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.trungld.viberide.presentation.screens.shared.components.ProfilePicture
import com.trungld.viberide.presentation.screens.shared.components.SearchBar
import com.trungld.viberide.presentation.viewmodels.AuthState

@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    authState: AuthState?,
    signOut: () -> Unit,
    logIn: () -> Unit,
    signUp: () -> Unit,
    onSearch: (String) -> Unit,// just added
) {
    var showOptions by remember { mutableStateOf(false) }
    // Top bar
    Row(
        modifier = modifier
            .padding(20.dp)
            .background(Color.DarkGray),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        SearchBar(modifier = Modifier.weight(1f),
            onSearch = onSearch)
        Spacer(modifier = modifier.width(8.dp))
        ProfilePicture(
            modifier = modifier.size(70.dp),
            imageUrl = null,
            onClick = { showOptions = !showOptions }
        )

        if (showOptions) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { showOptions = false }
            ) {
                if (authState is AuthState.Authenticated) {
                    // User is logged in: Show Log Out button
                    DropdownMenuItem(
                        text = { Text("Log Out") },
                        onClick = signOut
                    )
                } else {
                    // User is not logged in: Show Log In and Sign Up buttons
                    DropdownMenuItem(
                        text = { Text("Log In") },
                        onClick = {
                            showOptions = false
                            logIn()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sign Up") },
                        onClick = {
                            showOptions = false
                            signUp()
                        }
                    )
                }
            }
        }

    }
}

//@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
//@Composable
//private fun TopAppBarPreview() {
//    TopAppBar(
//        authState = AuthState.Authenticated,
//        signOut = {},
//        logIn = {},
//        signUp = {},
//        onSearch = {}
//    )
//
//}