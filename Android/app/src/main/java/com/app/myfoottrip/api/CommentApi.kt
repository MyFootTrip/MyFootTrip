package com.app.myfoottrip.api

import com.app.myfoottrip.model.Board
import com.app.myfoottrip.model.Comment
import retrofit2.Response
import retrofit2.http.*

interface CommentApi {

    // 댓글 생성
    @POST("community/board/detail/{boardId}/comment/create/")
    suspend fun writeComment(@Path("boardId") boardId: Int, @Body comment: Comment): Response<Board>

    //댓글 수정
    @PUT("community/board/detail/{boardId}/comment/{commentId}/")
    suspend fun updateComment(@Path("boardId") boardId: Int, @Path("commentId") commentId: Int, @Body comment: Comment) : Response<Board>

    //댓글 삭제
    @DELETE("community/board/detail/{boardId}/comment/{commentId}/")
    suspend fun deleteComment(@Path("boardId") boardId: Int, @Path("commentId") commentId: Int) : Response<Board>
}