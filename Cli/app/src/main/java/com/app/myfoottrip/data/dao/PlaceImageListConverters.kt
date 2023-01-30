package com.app.myfoottrip.data.dao

import androidx.room.TypeConverter
import com.google.gson.Gson
import okhttp3.MultipartBody

class PlaceImageListConverters {
    @TypeConverter
    fun listToJson(value: List<MultipartBody.Part>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): List<MultipartBody.Part>? {
        return Gson().fromJson(value, Array<MultipartBody.Part>::class.java)?.toList()
    }

} // End of PlaceImageListConverters