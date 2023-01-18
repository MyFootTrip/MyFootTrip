package com.app.myfoottrip.data.dto

import okhttp3.MultipartBody

class Image @JvmOverloads constructor(
    var profile_image: MultipartBody.Part? = null
) {
}