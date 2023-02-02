package com.app.myfoottrip.data.dto

import java.util.*

data class Comment @JvmOverloads constructor(
    val commentId : Int, //댓글 아이디
    val boardId : Int, //게시물 아이디
    val profileImg : String?,
    val userId : Int, //사용자 아이디
    val nickname : String,
    val content : String,
    val writeDate : Date,
    val message: String,
    )
