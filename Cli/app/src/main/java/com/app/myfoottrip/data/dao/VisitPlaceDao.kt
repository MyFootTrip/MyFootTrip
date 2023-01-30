package com.app.myfoottrip.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.*
import com.app.myfoottrip.data.dto.VisitPlace

@Dao
interface VisitPlaceDao {

    @Insert(onConflict = REPLACE)
    fun insertVisitPlace(visit_place: VisitPlace) // 장소를 저장

    @Update
    fun update(visit_place: VisitPlace)

    @Delete
    fun delete(visit_place: VisitPlace)

    @Query("SELECT * FROM visit_place WHERE id = (:id)")
    fun getVisitPlace(id: Long): VisitPlace

    @Query("SELECT * FROM visit_place")
    fun getAll(): List<VisitPlace>

    @Query("SELECT * FROM visit_place order by date DESC limit 1 ")
    fun getLastOne(): VisitPlace? // 가장 최근 항목 하나를 가져옴

//    @Query("DELETE FROM visit_place")
//    suspend fun deleteAll() // 전체 삭제
//
//    @Query("select count(*) from visit_place")
//    suspend fun getCount(): Int

} // End of VisitPlaceDao interface
