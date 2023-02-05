package com.app.myfoottrip.data.dao

import androidx.room.TypeConverter
import com.google.gson.Gson
import okhttp3.MultipartBody

class PlaceImageListConverters {
    @TypeConverter
    fun listToJson(value: List<String>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): List<String>? {
        return Gson().fromJson(value, Array<String>::class.java)?.toList()
    }

} // End of PlaceImageListConverters