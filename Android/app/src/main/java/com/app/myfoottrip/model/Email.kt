package com.app.myfoottrip.model

data class Email @JvmOverloads constructor(
    var email: String? = null,
    var validateNumber: String? = null
) : java.io.Serializable