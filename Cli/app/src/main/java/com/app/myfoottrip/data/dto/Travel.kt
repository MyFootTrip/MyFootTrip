package com.app.myfoottrip.data.dto

import java.util.Date

data class Travel @JvmOverloads constructor(
    val userId : Int,
    val travelId : Int?,
    val location : ArrayList<String>,
    val startDate : Date,
    val endDate : Date,
    val placeList : ArrayList<Place>
)
