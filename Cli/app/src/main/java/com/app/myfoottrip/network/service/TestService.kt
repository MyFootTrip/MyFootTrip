package com.app.myfoottrip.network.service

import com.app.myfoottrip.data.dto.Board
import retrofit2.Response
import retrofit2.http.Query

interface TestService {

    fun getBoard(@Query("size") size: Int,
                         @Query("page") page: Int): Response<ArrayList<Board>>
}