package com.app.myfoottrip.data.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Location (
    @PrimaryKey val id : Int?, //PK
    val placeId : Int?, //기존에 있는 값이면 placeId가 있고 새로 생성하는 것이면 null
    @ColumnInfo(name = "lat") val lat : Double, //위도
    @ColumnInfo(name = "lng") val lng : Double, //경도
    val time : Long, //기록 시간
    val address : String //주소
)