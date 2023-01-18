package com.app.myfoottrip.data.dto

import okhttp3.MultipartBody

data class JoinTest @JvmOverloads constructor(
    var email: String = "", //아이디
    var password: String = "",
    var username: String = "",
    var nickname: String = "",
    var profile_image: MultipartBody.Part? = null,
    var age: Int = 0, //연령대
    val kakao: String? = null, //카카오 이메일
    val naver: String? = null, //네이버 이메일
    val google: String? = null //구글 이메일
) : java.io.Serializable