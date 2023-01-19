package com.app.myfoottrip.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.app.myfoottrip.data.dto.Location

@Dao
interface LocationDao {
    @Query("SELECT * FROM location")
    fun getAll(): List<Location>

    @Insert
    fun insertLocation(location: Location)

    @Query("DELETE FROM location")
    fun deleteAll()

    @Query("select count(*) from location")
    fun getCount() : Int
}