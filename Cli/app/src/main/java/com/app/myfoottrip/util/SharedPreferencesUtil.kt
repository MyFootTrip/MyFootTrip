package com.app.myfoottrip.util

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesUtil(context: Context) {

    var preferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    companion object {
        const val SHARED_PREFERENCES_NAME = "myfoottrip_preference"
        const val COOKIES_KEY_NAME = "cookies"
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val LATITUDE = "latitude"
        const val LONITUDE = "longitude"
    }

    fun addUserRefreshToken(refresh_token: String) {
        val editor = preferences.edit()
        editor.putString(REFRESH_TOKEN, refresh_token)
        editor.apply()
    } // End of addUserRefreshToken

    fun getUserRefreshToken(): String {
        return preferences.getString(REFRESH_TOKEN, "").toString()
    } // End of getUserRefreshToken

    fun addUserAccessToken(access_token: String) {
        val editor = preferences.edit()
        val temp = "Bearer $access_token"
        editor.putString(ACCESS_TOKEN, temp)
        editor.apply()
    }

    fun getUserAccessToken(): String {
        return preferences.getString(ACCESS_TOKEN, "").toString()
    } // End of getUserAccessToken

    fun deleteUser() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    } // End of deleteUser

    fun addUserCookie(cookies: HashSet<String>) {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    } // End of addUserCookie

    fun getUserCookie(): MutableSet<String>? {
        return preferences.getStringSet(COOKIES_KEY_NAME, HashSet())
    } // End of getUserCookie

    fun deleteUserCookie() {
        preferences.edit().remove(COOKIES_KEY_NAME).apply()
    } // End of deleteUserCookie

    fun addLatitude(latitude: Double) {
        val editor = preferences.edit()
        editor.putLong(LATITUDE, latitude.toRawBits())
        editor.apply()
    } // End of addLatitude

    fun addLongitude(longitude: Double) {
        val editor = preferences.edit()
        editor.putLong(LONITUDE, longitude.toRawBits())
        editor.apply()
    } // End of addLongitude

    fun getLatitude(): Double {
        return Double.fromBits(preferences.getLong(LATITUDE, 0L))
    } // End of addLatitude

    fun getLongitude(): Double {
        return Double.fromBits(preferences.getLong(LONITUDE, 0L))
    } // End of addLongitude


} // End of SharedPreferencesUtil class