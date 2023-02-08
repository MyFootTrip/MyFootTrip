package com.app.myfoottrip.data.dao

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import okhttp3.MultipartBody

class PlaceImageListConverters {
    @TypeConverter
    fun listToJson(value: List<Uri>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): List<Uri>? {
        return Gson().fromJson(value, Array<Uri>::class.java)?.toList()
    }

} // End of PlaceImageListConverters