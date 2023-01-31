package com.app.myfoottrip.network.service

import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Filter
import com.app.myfoottrip.data.dto.Join
import com.app.myfoottrip.data.dto.Travel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BoardService {

    // 전체 게시물 조회
    @GET("community/board/")
    suspend fun getBoardList(): Response<ArrayList<Board>>

    //게시물 삽입
    @POST("community/board/create/")
    suspend fun writeBoard(@Body board: Board) : Response<Board>

    //게시물 필터
    @POST("community/board/filter/")
    suspend fun getFilteredBoardList(@Body filter: Filter) : Response<ArrayList<Board>>

    //게시물 좋아요
    @POST("/community/board/like/{boardId}/")
    suspend fun likeBoard(@Path("boardId") boardId: Int): Response<Boolean>

    //게시물 조회
    @GET("/community/board/detail/{boardId}/")
    suspend fun getBoard(@Path("boardId") boardId: Int): Response<Board>

}