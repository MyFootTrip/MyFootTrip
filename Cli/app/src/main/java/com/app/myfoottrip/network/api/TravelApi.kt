package com.app.myfoottrip.network.api

import com.app.myfoottrip.data.dto.Travel
import retrofit2.Response
import retrofit2.http.*

interface TravelApi {
    // 여정 생성
    @POST("/community/travel/create/")
    suspend fun createTravel(@Body travel: Travel): Response<Void>

    //유저별 여정 조회
    @GET("/community/travel/user/{userId}")
    suspend fun getUserTravel(@Path("userId") userId: Int): Response<ArrayList<Travel>>

    // 여정 조회
    @GET("/community/traveldetail/{travelId}")
    suspend fun getTravel(@Path("travelId") travelId: Int): Response<Travel>


    // 여정 수정
    @PUT("/community/traveldetail/{travelId}")
    suspend fun updateTravel(@Path("travelId") travelId: Int, @Body travel: Travel): Response<Unit>
} // End of TravelApi Interface
