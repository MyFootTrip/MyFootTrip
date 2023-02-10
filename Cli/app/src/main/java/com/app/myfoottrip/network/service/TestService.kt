package com.app.myfoottrip.network.service

import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Filter
import com.app.myfoottrip.data.dto.TestResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TestService {

    @GET("/community/board/test/")
    suspend fun getBoardList(@Query("page") page: Int,
                             @Query("peroidList") periodList: ArrayList<String>,
                             @Query("ageList") ageList: ArrayList<String>,
                             @Query("themeList") themeList: ArrayList<String>,
                             @Query("regionList") regionList: ArrayList<String>,
                             @Query("sortedType") sortedType: Int): Response<List<Board>>
}