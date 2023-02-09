package com.app.myfoottrip.data.dao

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import okhttp3.MultipartBody

class PlaceImageListConverters {
    @TypeConverter
    fun listToJson(value: MutableList<String>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): MutableList<String>? {
        return Gson().fromJson(value, Array<String>::class.java)?.toMutableList()
    }

} // End of PlaceImageListConverters