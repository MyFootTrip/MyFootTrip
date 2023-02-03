package com.app.myfoottrip.network.service

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.PUT

interface FcmService {

    //FCM 토큰 저장
    @FormUrlEncoded
    @POST("/accounts/firebase_save/")
    suspend fun addFcmToken(@Field("firebaseToken") firebaseToken : String) : Response<String>

    //FCM 토큰 삭제
    @FormUrlEncoded
    @PUT("/accounts/firebase_save/")
    suspend fun deleteFcmToken(@Field("firebaseToken") firebaseToken: String) : Response<String>
}