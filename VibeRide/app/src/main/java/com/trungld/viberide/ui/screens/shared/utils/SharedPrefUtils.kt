package com.trungld.viberide.ui.screens.shared.utils

import android.content.Context

object SharedPrefUtils {

    // Save a search query to SharedPreferences
    fun saveSearchQuery(context: Context, query: String) {
        val prefs = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
        val history = prefs.getStringSet("history", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        history.add(query)
        prefs.edit().putStringSet("history", history).apply()
    }

    // Retrieve the search history
    fun getSearchHistory(context: Context): List<String> {
        val prefs = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
        return prefs.getStringSet("history", emptySet())?.toList() ?: emptyList()
    }
}