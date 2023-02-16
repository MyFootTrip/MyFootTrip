package com.app.myfoottrip.api

import com.app.myfoottrip.model.Board
import com.app.myfoottrip.model.Filter
import retrofit2.Response
import retrofit2.http.*

interface BoardApi {

    // 전체 게시물 조회
    @GET("/community/board/")
    suspend fun getBoardList(@Query("page") page: Int,
                             @Query("peroidList") periodList: ArrayList<String>,
                             @Query("ageList") ageList: ArrayList<String>,
                             @Query("themeList") themeList: ArrayList<String>,
                             @Query("regionList") regionList: ArrayList<String>,
                             @Query("sortedType") sortedType: Int): Response<List<Board>>

    //게시물 삽입
    @POST("community/board/create/")
    suspend fun writeBoard(@Body board: Board) : Response<Board>

    //게시물 필터
    @POST("community/board/filter/")
    suspend fun getFilteredBoardList(@Body filter: Filter) : Response<ArrayList<Board>>

    //게시물 좋아요
    @FormUrlEncoded
    @POST("/community/board/like/{boardId}/")
    suspend fun likeBoard(@Path("boardId") boardId: Int, @Field("message") message: String): Response<Boolean>

    //게시물 조회
    @GET("/community/board/detail/{boardId}/")
    suspend fun getBoard(@Path("boardId") boardId: Int): Response<Board>

    //게시물 수정
    @PUT("/community/board/detail/{boardId}/")
    suspend fun updateBoard(@Path("boardId") boardId: Int, @Body board: Board): Response<Board>

    //게시물 삭제
    @DELETE("/community/board/detail/{boardId}/")
    suspend fun deleteBoard(@Path("boardId") boardId: Int) : Response<String>

    //내가 작성한 게시물 전체 리스트 조회
    @GET("/community/board/user/")
    suspend fun getWriteBoardList() : Response<ArrayList<Board>>

    //내가 좋아요한 게시물 리스트 조회
    @GET("/community/board/user/like/")
    suspend fun getLikeBoardList() : Response<ArrayList<Board>>

}