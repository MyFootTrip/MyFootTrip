package com.app.myfoottrip.data.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.myfoottrip.data.dto.VisitPlace

@TypeConverters(PlaceImageListConverters::class)
@Database(entities = [VisitPlace::class], version = 1)
abstract class TravelDatabase : RoomDatabase() {
    abstract fun visitPlaceDao(): VisitPlaceDao

} // End of TravelDatabase