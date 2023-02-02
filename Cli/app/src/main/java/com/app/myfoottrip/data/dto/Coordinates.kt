package com.app.myfoottrip.data.dto

data class Coordinates @JvmOverloads constructor(
    var latitude: Double? = null,
    var longitude: Double? = null
) : java.io.Serializable