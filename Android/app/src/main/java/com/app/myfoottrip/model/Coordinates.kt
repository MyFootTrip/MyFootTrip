package com.app.myfoottrip.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class Coordinates @JvmOverloads constructor(
    var latitude: Double? = null,
    var longitude: Double? = null
) : java.io.Serializable