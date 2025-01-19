package com.trungld.viberide.ui.screens.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.trungld.viberide.R
import com.trungld.viberide.ui.theme.VibeRideTheme

@Composable
fun SearchBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(61.dp)
            .padding(2.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)) // Apply shadow with rounded corners
            .background(
                color = if (isSystemInDarkTheme()) Color.Black else Color.White,
            )
    ) {
        var searchText by remember { mutableStateOf("") }
        TextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textStyle = TextStyle(fontSize = 19.sp),
            leadingIcon = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search), // Replace with your search icon
                        contentDescription = "Search",
                    )
                }
            },
            placeholder = { Text(text = "Search", color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent, // Set the container color to transparent
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent, // Remove the focused indicator
                unfocusedIndicatorColor = Color.Transparent, // Remove the unfocused indicator
                disabledIndicatorColor = Color.Transparent, // Remove the disabled indicator
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    VibeRideTheme() {
        SearchBar()
    }
}