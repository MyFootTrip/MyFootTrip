package com.app.myfoottrip.model

data class Token @JvmOverloads constructor(
    var access_token: String? = "",
    var refresh_token: String? = ""
)
