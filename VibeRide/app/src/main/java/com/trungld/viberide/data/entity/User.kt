package com.trungld.viberide.data.entity

data class User(
    val id: String? = null,
    val username: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
