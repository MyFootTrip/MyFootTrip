package com.app.myfoottrip.network.service

import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.TestResponse
import retrofit2.Response
import retrofit2.http.GET

interface TestService {

    @GET("/community/board/")
    suspend fun getBoardList(): Response<List<Board>>
}