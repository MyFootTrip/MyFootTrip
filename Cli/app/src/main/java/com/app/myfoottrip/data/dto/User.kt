package com.app.myfoottrip.data.dto

data class User @JvmOverloads constructor(
val uid: Int,
val join: Join? = null,
val travel: ArrayList<Travel>? = null,
val myLikeBoard: ArrayList<Board>? = null,
val writeBoard: ArrayList<Board>? = null,
val commentList: ArrayList<Comment>? = null
) // End of User
