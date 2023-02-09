package com.app.myfoottrip.data.dto

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.util.LinkedList

@Entity(tableName = "visit_place")
data class VisitPlace(
    @NotNull val number: Int,
    val address: String, //수정 가능한 주소
    val lat: Double, //위도
    val lng: Double, //경도
    val date: Long? = 0, //기록 시간
    var imgList: MutableList<Uri> = LinkedList(),
    val content: String? = "",
    val placeName: String? = ""
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
        imgList: MutableList<Uri>,
        content: String?,
        placeName: String?
    ) : this(number, address, lat, lng, date, imgList, content, placeName) {
        this.ID = id;
    }
}
