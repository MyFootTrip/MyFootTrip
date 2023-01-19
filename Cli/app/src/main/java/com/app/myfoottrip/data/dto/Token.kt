package com.app.myfoottrip.data.dto

data class Token @JvmOverloads constructor(
    var access_token: String? = "",
    var refresh_token: String? = ""
)
