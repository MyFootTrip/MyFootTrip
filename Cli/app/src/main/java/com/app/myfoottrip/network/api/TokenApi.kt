package com.app.myfoottrip.network.api

import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.data.dto.User
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface TokenApi {
    // refreshToken을 제출해서 accessToken을 발급받는 API
    @POST("token/refresh/")
    suspend fun refreshTokenAvailableCheck(@Body refreshToken: Token): Response<Token>

    // access_token을 헤더에 담아서 보내기
    @GET("accounts/jwtdetail/")
    suspend fun getUserDataByAccessToken(): Response<User>

    //refresh 토큰 삭제 요청
    @FormUrlEncoded
    @POST("token/blacklist/")
    suspend fun deleteRefreshToken(@Field("refresh_token") refreshToken: String): Response<String>

    // NaverLogin Token보내기
    @POST("accounts/social_login/naver/")
    suspend fun postNaverLoginAccessToken(@Body token: JsonObject): Response<Token>

    // Kakao Login Token 보내기
    @POST("accounts/social_login/kakao/")
    suspend fun postKakaoLoginAccessToken(@Body token: JsonObject): Response<Token>

    // Google Login Token 보내기
    @POST("accounts/social_login/google/")
    suspend fun postGoogleLoignAccessToken(@Body token: JsonObject): Response<Token>

} // End of TokenApi Interface
