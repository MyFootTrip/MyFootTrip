package com.app.myfoottrip.api

import com.app.myfoottrip.model.Email
import com.app.myfoottrip.model.Join
import com.app.myfoottrip.model.Token
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
        @Part profile_image: MultipartBody.Part?,
        @PartMap join: HashMap<String, RequestBody>,
    ): Response<Token>

    //유저 정보 수정
    @Multipart
    @PUT("/accounts/userdetail/")
    suspend fun updateUser(@Part profile_image: MultipartBody.Part?, @PartMap join: HashMap<String,RequestBody>) : Response<Join>

    // 이메일 인증번호 입력
    @POST("accounts/emailvalidate/")
    suspend fun emailValidateCheck(
        @Body emailId: Email
    ): Response<Boolean>

    // 로그인
    @POST("login/")
    suspend fun userLogin(
        @Body userLoginData: JsonObject
    ): Response<Token>

    //아이디 변경
    @FormUrlEncoded
    @POST("accounts/changeEmail/")
    suspend fun updateId(@Field("email") email:String): Response<Void>

    //비밀번호 변경
    @FormUrlEncoded
    @POST("/accounts/changePassword/")
    suspend fun updatePassword(@Field("new_password1") newPassword1: String, @Field("new_password2") newPassword2: String): Response<Void>

}// End of UserApi Interface
