package com.app.myfoottrip.data.dto

import java.util.Date

data class Alarm @JvmOverloads constructor(
    val notificationId: Int,
    var notificationType: Int, //0: 좋아요 1: 댓글
    var profileImg: String = "",
    var message: String = "",
    var createDate: Date,
)
