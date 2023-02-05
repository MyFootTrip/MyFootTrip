package com.app.myfoottrip.data.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.*
import com.app.myfoottrip.data.dto.VisitPlace

@Dao
interface VisitPlaceDao {

    /*
        onConflict 속성을 지정할 수 있다.
        테이블에 Entity를 삽입할 때 같은 값인 경우, 충돌이 발생하는데 이 충돌을 어떻게 해결할지를 정의할 수 있다.
     */
    @Insert(onConflict = REPLACE)
    fun insertVisitPlace(visit_place: VisitPlace) : Long // 장소를 저장

    @Update
    fun update(visit_place: VisitPlace)

    @Delete
    fun delete(visit_place: VisitPlace)

    @Query("SELECT * FROM visit_place WHERE id = (:id)")
    fun getVisitPlace(id: Long): VisitPlace

    @Query("SELECT * FROM visit_place")
    fun getAll(): List<VisitPlace>

    @Query("SELECT * FROM visit_place order by date DESC limit 1 ")
    fun getMostRecentVisitPlace(): VisitPlace? // 가장 최근 항목 하나를 가져옴

    // 테이블의 데이터 전체 삭제
    @Query("DELETE FROM visit_place")
    fun deleteAllData()
} // End of VisitPlaceDao interface
