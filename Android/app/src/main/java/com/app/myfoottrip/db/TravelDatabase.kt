package com.app.myfoottrip.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.myfoottrip.model.VisitPlace

@TypeConverters(PlaceImageListConverters::class)
@Database(entities = [VisitPlace::class], version = 1)
abstract class TravelDatabase : RoomDatabase() {
    abstract fun visitPlaceDao(): VisitPlaceDao

} // End of TravelDatabase