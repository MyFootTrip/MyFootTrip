package com.app.myfoottrip.data.dto

import java.util.Date

data class Travel @JvmOverloads constructor(
    var travelId: Int? = 0,
    var location: ArrayList<String>? = null,
    var startDate: Date? = null,
    var endDate: Date? = null,
    var placeList: ArrayList<Place>? = null
)
