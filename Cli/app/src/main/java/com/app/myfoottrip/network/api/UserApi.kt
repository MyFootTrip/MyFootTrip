package com.app.myfoottrip.network.api

import com.app.myfoottrip.data.dto.Email
import com.app.myfoottrip.data.dto.Join
import com.app.myfoottrip.data.dto.JoinTest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {

    // 사용자 이메일 사용 여부 체크
    @POST("accounts/emailcheck/")
    suspend fun isUsedEmailId(
        @Body emailId: Email
    ): Response<Boolean>

    // 사용자 회원 가입
    @POST("registration/")
    suspend fun userJoin(
        @Body joinUserData: Join
    ): Response<JoinTest>

    // 이메일 인증번호 입력
    @POST("accounts/emailvalidate/")
    suspend fun emailValidateCheck(
        @Body emailId: Email
    ): Response<Boolean>

} // End of UserApi class