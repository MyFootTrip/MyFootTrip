package com.app.myfoottrip.data.dto

import java.util.*
import kotlin.collections.ArrayList


data class Place(
    val placeId : Int, //장소 아이디
    val placeName : String, //장소 이름
    val saveDate : Date, //기록 시간
    val memo : String, // 기록
    val placeImgList : ArrayList<String>, //장소 사진 리스트
    val latitude : Double, //위도
    val longitude : Double, //경도
    val address : String, //해당 위치 주소
)
