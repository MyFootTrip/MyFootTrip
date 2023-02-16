package com.app.myfoottrip.api

import com.app.myfoottrip.model.Travel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface TravelApi {
    // 여정 생성
    @Multipart
    @JvmSuppressWildcards
    @POST("/community/travel/create/")
    suspend fun createTravel(
        @Part imageList: List<MultipartBody.Part?>,
        @PartMap travel : HashMap<String, RequestBody>
    ): Response<Void>

    // 생성 테스트
    @Multipart
    @JvmSuppressWildcards
    @POST("/accounts/testing/")
    suspend fun createTravelTest(
        @Part imageList: List<MultipartBody.Part?>,
    ): Response<Void>

    //유저별 여정 조회
    @GET("/community/travel/user/{userId}")
    suspend fun getUserTravel(@Path("userId") userId: Int): Response<ArrayList<Travel>>

    // 여정 조회
    @GET("/community/travel/detail/{travelId}")
    suspend fun getTravel(@Path("travelId") travelId: Int): Response<Travel>

    // 여정 수정
    @Multipart
    @JvmSuppressWildcards
    @PUT("/community/travel/detail/{travelId}/")
    suspend fun updateTravel(
        @Path("travelId") travelId: Int,
        @Part newImageList: List<MultipartBody.Part?>,
        @PartMap updateTravelRequestHashMap : HashMap<String, RequestBody>
    ): Response<Travel>

    // 여정 삭제
    @DELETE("/community/travel/detail/{travelId}/")
    suspend fun deleteTravel(@Path("travelId") travelId: Int): Response<Void>

} // End of TravelApi Interface
