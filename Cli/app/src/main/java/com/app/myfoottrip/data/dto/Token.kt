package com.app.myfoottrip.data.dto

data class Token @JvmOverloads constructor(
    var accessToken: String,
    var refreshToken: String
)
