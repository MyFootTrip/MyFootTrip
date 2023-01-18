package com.app.myfoottrip.data.dto

import java.util.Date

data class Travel @JvmOverloads constructor(
    val travelId : String,
    val location : String,
    val startDate : Date,
    val endDate : Date,
    val placeList : ArrayList<Place>
)
