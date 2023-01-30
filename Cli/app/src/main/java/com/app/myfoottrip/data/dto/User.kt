package com.app.myfoottrip.data.dto

data class User @JvmOverloads constructor(
    var uid: Int,
    var join: Join,
    var travel: List<Travel>,
    var myLikeBoard: List<Board>,
    var writeBoard: List<Board>,
    var commentList: List<Comment>
) // End of User
