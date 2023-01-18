package com.app.myfoottrip.data.model.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Location (
    @PrimaryKey val id : Int?,
    @ColumnInfo(name = "lat") val lat : Double,
    @ColumnInfo(name = "lng") val lng : Double,
    val time : Long
)