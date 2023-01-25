package com.app.myfoottrip.data.dto

data class Email @JvmOverloads constructor(
    var email: String? = null,
    var validateNumber: String? = null
) : java.io.Serializable