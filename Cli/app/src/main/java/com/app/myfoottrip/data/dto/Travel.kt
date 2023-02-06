package com.app.myfoottrip.data.dto

import java.util.*

data class Travel @JvmOverloads constructor(
    var travelId: Int? = 0,
    var location: List<String> = emptyList(),
    var startDate: Date? = null,
    var endDate: Date? = null,
    var placeList: List<Place>? = null
)
