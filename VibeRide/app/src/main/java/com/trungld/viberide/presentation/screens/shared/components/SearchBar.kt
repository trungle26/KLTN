package com.trungld.viberide.presentation.screens.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.trungld.viberide.R
import com.trungld.viberide.presentation.theme.VibeRideTheme

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit
) {
    Box(
        modifier = modifier
            .height(61.dp)
            .padding(2.dp)
            .background(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            ),
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
                IconButton(onClick = {
                    if (searchText.isNotEmpty()) onSearch(searchText)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search), // Replace with your search icon
                        contentDescription = "Search",
                    )
                }
            },
            placeholder = { Text(text = "Search", color = Color.LightGray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent, // Set the container color to transparent
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent, // Remove the focused indicator
                unfocusedIndicatorColor = Color.Transparent, // Remove the unfocused indicator
                disabledIndicatorColor = Color.Transparent, // Remove the disabled indicator
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // Set enter key to "Search"
            keyboardActions = KeyboardActions(
                onDone = {
                    if (searchText.isNotEmpty()) onSearch(searchText) // Trigger search on enter
                }
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    VibeRideTheme() {
        SearchBar(Modifier.width(300.dp),{})
    }
}