package com.app.myfoottrip.network.service

import com.app.myfoottrip.data.dto.Join
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {

    // 로그인
    @POST("login/")
    suspend fun login(@Body body: Join): Boolean
}