package com.app.myfoottrip.network.api

import com.app.myfoottrip.data.dto.Token
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenApi {

    // refreshToken을 제출해서 accessToken을 발급받는 API
    @POST("token/refresh/")
    suspend fun refreshTokenAvailableCheck(
        @Body refresh_token: Token
    ): Response<Token>

}