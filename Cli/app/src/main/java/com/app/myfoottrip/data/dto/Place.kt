package com.app.myfoottrip.data.dto

import ted.gun0912.clustering.clustering.TedClusterItem
import ted.gun0912.clustering.geometry.TedLatLng
import java.util.*
import kotlin.collections.ArrayList

data class Place @JvmOverloads constructor(
    var placeId: Int? = 0, //장소 아이디
    var placeName: String? = "", //장소 이름
    var saveDate: Date? = null, //기록 시간
    var memo: String? = "", // 기록
    var placeImgList: ArrayList<String>? = null, //장소 사진 리스트
    var latitude: Double? = 0.0, //위도
    var longitude: Double? = 0.0, //경도
    var address: String? = "", //해당 위치 주소
) : java.io.Serializable, TedClusterItem {
    override fun getTedLatLng(): TedLatLng {
        return TedLatLng(latitude!!, longitude!!)
    }
}
