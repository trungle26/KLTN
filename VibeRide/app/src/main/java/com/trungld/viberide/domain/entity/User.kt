package com.trungld.viberide.domain.entity

data class User(
    val id: String? = null,
    val username: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
