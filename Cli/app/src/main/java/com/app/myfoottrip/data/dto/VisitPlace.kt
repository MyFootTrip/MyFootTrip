package com.app.myfoottrip.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import okhttp3.MultipartBody
import org.jetbrains.annotations.NotNull
import java.util.*

@Entity(tableName = "visit_place")
data class VisitPlace(
    @NotNull val number: Int,
    val address: String, //수정 가능한 주소
    val lat: Double, //위도
    val lng: Double, //경도
    val date: Long? = 0, //기록 시간
    val imgList: List<String> = emptyList()
) {
    @PrimaryKey(autoGenerate = true)
    var ID: Long = 0L //PK

    constructor(
        id: Long,
        number: Int,
        address: String,
        lat: Double,
        lng: Double,
        date: Long,
        imgList: List<String>
    ) : this(number, address, lat, lng, date, imgList) {
        this.ID = id;
    }
}
