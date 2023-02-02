package com.app.myfoottrip.network.service

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface FcmService {
    //FCM 토큰
    @FormUrlEncoded
    @POST("/accounts/firebase_save/")
    suspend fun addFcmToken(@Field("firebaseToken") firebaseToken : String) : Response<String>
}