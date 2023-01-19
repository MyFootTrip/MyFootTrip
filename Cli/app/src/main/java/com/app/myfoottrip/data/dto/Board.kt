package com.app.myfoottrip.data.dto

import java.util.Date


data class Board @JvmOverloads constructor(
    val boardId: Int, //게시글 번호
    val userId : Int,
    val nickname : String,
    val profileImg : String, //프로필 이미지
    var writeDate : Date, //작성 날짜
    val theme : String, //여행 테마(복수선택 가능)
    val title : String,
    val content: String,
    var imageList : ArrayList<String>,
    val travel : Travel?, //여정 정보 객체
    var likeCount : Int = 0, //좋아요 수
    var commentCount : Int = 0, //댓글 수
)
