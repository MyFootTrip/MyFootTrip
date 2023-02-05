package com.app.myfoottrip.data.dto

import java.util.Date


data class Board @JvmOverloads constructor(
    val boardId: Int, //게시글 번호
    val userId : Int,
    val nickname : String,
    val profileImg : String, //프로필 이미지
    var writeDate : Date, //작성 날짜
    var theme : String, //여행 테마
    var title : String,
    var content: String,
    var imageList : ArrayList<String>,
    val travel : Travel?, //여정 정보 객체
    var likeList : ArrayList<Int>, //좋아요한 유저 정보
    var commentList : ArrayList<Comment>, //댓글 수
)
