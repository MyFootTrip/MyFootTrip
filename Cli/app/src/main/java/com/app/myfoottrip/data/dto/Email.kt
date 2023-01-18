package com.app.myfoottrip.data.dto

data class Email @JvmOverloads
constructor(
    var email: String = "",
    var validateNumber: String = ""
) : java.io.Serializable
