package com.app.myfoottrip.data.dto

import java.util.Date

data class Travel @JvmOverloads constructor(
    val travelId : String,
    val location : String,
    val startDate : Date,
    val endDate : Date,
    val theme : String, //여행 테마(복수선택 가능)
    val placeList : ArrayList<Place>
)
