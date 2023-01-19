package com.app.myfoottrip.data.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.myfoottrip.data.dao.LocationDao
import com.app.myfoottrip.data.dto.Location

@Database(entities = [Location::class], version = 2)
abstract class AppDatabase : RoomDatabase(){
    abstract fun locationDao(): LocationDao

    companion object{
        private var DATABASE : AppDatabase? = null

        fun getInstance(context : Context) :AppDatabase{
            DATABASE ?: synchronized(this){
                DATABASE?: buildDatabase(context).also {
                    DATABASE = it
                }
            }
            return DATABASE!!
        }

        private fun buildDatabase(context : Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "LocationDB"
            ).fallbackToDestructiveMigration()
            .build()
    }
}