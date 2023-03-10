package com.app.myfoottrip.model

import androidx.annotation.Keep
import okhttp3.MultipartBody
import java.util.*

@Keep
data class PlacePush @JvmOverloads constructor(
    var placeId: Int? = 0, //장소 아이디
    var placeName: String? = "", //장소 이름
    var saveDate: Date? = null, //기록 시간
    var memo: String? = "", // 기록
    var placeImgList: MutableList<MultipartBody.Part>? = LinkedList(), //장소 사진 리스트
    var latitude: Double? = 0.0, //위도
    var longitude: Double? = 0.0, //경도
    var address: String? = "", //해당 위치 주소
) : java.io.Serializable // End of TravelPush
