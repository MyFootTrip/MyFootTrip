package com.app.myfoottrip.data.dto

import androidx.annotation.Keep
import java.util.*

@Keep
data class TravelPush @JvmOverloads constructor(
    var travelId: Int? = 0,
    var location: List<String> = emptyList(),
    var startDate: Date? = null,
    var endDate: Date? = null,
    var placeList: List<PlacePush>? = null
) : java.io.Serializable // End of TravelPush
