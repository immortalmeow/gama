package com.android.fitmoveai.core.utils

data class UserModel(
    val username: String,
    val email: String,
    val password: String,
    val isLogin: Boolean,
)

data class UserToken(
    val token: String,
)