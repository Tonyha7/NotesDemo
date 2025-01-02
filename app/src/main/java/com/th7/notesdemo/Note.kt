package com.th7.notesdemo

data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val author: String,
    val image: String?,
    val createdAt: String,
    val updateAt: String,
    val deleted: Int
)

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T
)
