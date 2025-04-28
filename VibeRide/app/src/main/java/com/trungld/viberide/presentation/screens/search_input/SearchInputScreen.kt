package com.trungld.viberide.presentation.screens.search_input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.trungld.viberide.domain.entity.SearchEntry
import com.trungld.viberide.presentation.theme.VibeRideTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchInputScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    pastSearches: List<SearchEntry> = emptyList(),
    loadSearchHistory: () -> Unit,
    saveSearch: (String) -> Unit,
    onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Trigger focus request when the screen enters composition
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        loadSearchHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search") },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.DarkGray.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.DarkGray.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (searchQuery.isNotBlank()) {
                                    saveSearch(searchQuery)
                                    onSearch(searchQuery)
                                }
                            }) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // Set enter key to "Search"
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (searchQuery.isNotBlank()) {
                                    saveSearch(searchQuery)
                                    onSearch(searchQuery)
                                }
                            }
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.DarkGray.copy(alpha = 0.9f),
                    titleContentColor = Color.White
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .padding(paddingValues)
                .background(Color.Black.copy(alpha = 0.9f))
        ) {
            items(pastSearches) { search ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { // Apply clickable to the whole Row
                            searchQuery = search.query
                            saveSearch(search.query) // Resave to update timestamp
                            onSearch(search.query)
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp), // Adjust padding as needed
                    verticalAlignment = Alignment.CenterVertically // Align icon and text vertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History, // Use History icon
                        contentDescription = "Search history item",
                        tint = Color.Gray // Use a less prominent color for the icon
                    )
                    Spacer(modifier = Modifier.width(16.dp)) // Add space between icon and text
                    Text(
                        text = search.query,
                        color = Color.White,
                        fontSize = 18.sp,
                    )
                }
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            }
            if (pastSearches.isEmpty()) {
                item {
                    Text(
                        text = "No recent searches",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SearchInputPreview() {
    VibeRideTheme {
        SearchInputScreen(
            modifier = Modifier
                .height(200.dp)
                .width(400.dp),
            navController = rememberNavController(),
            loadSearchHistory = {},
            saveSearch = {},
            onSearch = {},
            pastSearches = listOf(SearchEntry("Test", System.currentTimeMillis()))
        )
    }
}