package com.app.myfoottrip.data.dao

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import com.app.myfoottrip.data.dto.VisitPlace

private const val TAG = "VisitPlaceRepository"
private const val DATABASE_NAME = "visit-place-database.db"

class VisitPlaceRepository private constructor(context: Context) {
    private val database: TravelDatabase = Room.databaseBuilder(
        context.applicationContext, // context
        TravelDatabase::class.java, // 어떤 클래스로 데이터 베이스를 만들건지.
        DATABASE_NAME // 데이터 베이스 이름은 뭔지 (파일 이름을 뭘로 만들지)
    ).build()

    // DB테이블 생성하고, Dao까지 모두 만들어준다. NoteDao를 return한다.
    private val visitPlaceDao = database.visitPlaceDao()

    suspend fun getAllVisitPlace(): List<VisitPlace> {
        return visitPlaceDao.getAll()
    } // End of getVisitPlaces

    suspend fun getVisitPlace(id: Long): VisitPlace {
        return visitPlaceDao.getVisitPlace(id)
    } // End of getVisitPlaces

    suspend fun getMostRecentVisitPlace(): VisitPlace? {
        return visitPlaceDao.getMostRecentVisitPlace()
    } // End of getMostRecentVisitPlace

    suspend fun insertVisitPlace(dto: VisitPlace): Long = database.withTransaction {
        visitPlaceDao.insertVisitPlace(dto)
    } // End of insertVisitPlace

    suspend fun updateVisitPlace(dto: VisitPlace) = database.withTransaction {
        visitPlaceDao.update(dto)
    } // End of updateVisitPlace

    suspend fun deleteVisitPlace(dto: VisitPlace) = database.withTransaction {
        visitPlaceDao.delete(dto)
    } // End of deleteVisitPlace

    suspend fun deleteAllVisitPlace() = database.withTransaction {
        visitPlaceDao.deleteAllData()
    } // End of deleteAllVisitPlace

    // 싱글톤을 위한 구현
    companion object {
        private var INSTANCE: VisitPlaceRepository? = null

        // initialize를 통해서 객체를 불러옴.
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = VisitPlaceRepository(context)
            }
        } // End of initialize

        fun get(): VisitPlaceRepository {
            return INSTANCE ?: throw IllegalStateException("NoteRepository must be initialized")
        } // End of get
    } // End of companion object
} // End of VisitPlaceRepository class
