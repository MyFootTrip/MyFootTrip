package com.app.myfoottrip.data.dto

import android.os.Parcel
import android.os.Parcelable

data class JoinTest @JvmOverloads constructor(
    var access_token: String? = "", var refresh_token: String? = "", var user: User? = null
) : java.io.Serializable