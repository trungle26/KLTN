package com.trungld.viberide.domain.entity

data class SearchEntry(
    val query: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
)