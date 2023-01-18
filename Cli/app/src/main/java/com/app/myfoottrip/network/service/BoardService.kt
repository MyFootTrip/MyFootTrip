package com.app.myfoottrip.network.service

import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Join
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BoardService {

    // 전체 게시물 조회
    @GET("community/board/")
    suspend fun getBoardList(): Response<ArrayList<Board>>

    //게시물 삽입
    @POST("community/board/create/")
    suspend fun writeBoard(@Body board: Board) : Response<Board>

}