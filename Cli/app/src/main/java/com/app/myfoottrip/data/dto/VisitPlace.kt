package com.app.myfoottrip.data.dto

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.util.LinkedList

/*
    Uri를 List로 RoomDB에서 저장할 수 없으므로
    Uri를 String 타입 List로 저장해놓고 꺼낼때 마다 Parse해서 사용해야됨
 */

@Entity(tableName = "visit_place")
data class VisitPlace(
    @NotNull val number: Int,
    var address: String, //수정 가능한 주소
    val lat: Double, //위도
    val lng: Double, //경도
    val date: Long? = 0, //기록 시간
    var imgList: MutableList<String> = LinkedList(),
    var content: String? = "",
    var placeName: String? = ""
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
        imgList: MutableList<String>,
        content: String?,
        placeName: String?
    ) : this(number, address, lat, lng, date, imgList, content, placeName) {
        this.ID = id;
    }
}
