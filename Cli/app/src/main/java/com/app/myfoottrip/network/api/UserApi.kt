package com.app.myfoottrip.network.api

import com.app.myfoottrip.data.dto.Email
import com.app.myfoottrip.data.dto.JoinTest
import com.app.myfoottrip.data.dto.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    // 사용자 이메일 사용 여부 체크
    @POST("accounts/emailcheck/")
    suspend fun isUsedEmailId(
        @Body emailId: Email
    ): Response<Boolean>

    // 사용자 회원 가입
    @Multipart
    @POST("registration/")
    suspend fun userJoin(
        @Part profile_image: MultipartBody.Part,
    ): Response<User>

    // 이메일 인증번호 입력
    @POST("accounts/emailvalidate/")
    suspend fun emailValidateCheck(
        @Body emailId: Email
    ): Response<Boolean>

    // 이미지 전송 테스트
    @Multipart
    @POST("accounts/image_test/")
    suspend fun imageTest(
        @Part profileImg: MultipartBody.Part,
    ): Response<User>


} // End of UserApi class