package com.app.myfoottrip.model

import androidx.annotation.Keep
import java.util.*

@Keep
data class TravelPush @JvmOverloads constructor(
    var travelId: Int? = 0,
    var location: List<String> = emptyList(),
    var startDate: Date? = null,
    var endDate: Date? = null,
    var placeList: List<PlacePush>? = null,
    var deletePlaceList: MutableList<String> = LinkedList(), // 삭제된 이미지 리스트 정보를 담고 있음 구분하지 말고 다본내라.
    var deleteImageList : MutableList<String> = LinkedList() // 삭제된 Place의 아이디를 담아서 보내라
) : java.io.Serializable // End of TravelPush
