package com.app.myfoottrip.network.api

import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface TokenApi {

    // refreshToken을 제출해서 accessToken을 발급받는 API
    @POST("token/refresh/")
    suspend fun refreshTokenAvailableCheck(
        @Body refreshToken: Token
    ): Response<Token>

    // access_token을 헤더에 담아서 보내기
    @GET("accounts/jwtdetail/")
    suspend fun getUserDataByAccessToken(): Response<Join>

}