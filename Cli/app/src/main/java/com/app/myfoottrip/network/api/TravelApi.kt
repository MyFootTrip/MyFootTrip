package com.app.myfoottrip.network.api

import com.app.myfoottrip.data.dto.Travel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TravelApi {
    // 여정 조회
    @GET("{travelId}")
    suspend fun getTravel(@Path("travelId") travelId : Int) : Response<Travel>

    // 여정 생성
    @POST("")
    suspend fun makeTravel(@Body travel: Travel) : Response<Unit>

    // 여정 수정
    @POST("")
    suspend fun updateTravel(@Body travel: Travel) : Response<Unit>

}