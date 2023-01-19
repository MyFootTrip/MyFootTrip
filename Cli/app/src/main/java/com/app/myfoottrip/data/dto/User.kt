package com.app.myfoottrip.data.dto

data class User @JvmOverloads constructor(
    val uid: String,
    val join: Join,
    val travel: ArrayList<Travel>,
    val myLikeBoard: ArrayList<Board>,
    val writeBoard: ArrayList<Board>,
    val totalDate: Int,
)
