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
        const val FCM_TOKEN = "fcm_token"
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

    fun deleteAccessToken(){
        preferences.edit().remove(ACCESS_TOKEN).apply()
    }

    fun deleteRefreshToken(){
        preferences.edit().remove(REFRESH_TOKEN).apply()
    }

//    fun deleteRefreshToken() {
//        val editor = preferences.edit()
//        editor.clear()
//        editor.apply()
//    } // End of deleteRefreshToken

    fun deleteUserCookie() {
        preferences.edit().remove(COOKIES_KEY_NAME).apply()
    } // End of deleteUserCookie

    fun addFcmToken(fcmToken: String){
        val editor = preferences.edit()
        editor.putString(FCM_TOKEN, fcmToken)
        editor.apply()
    }

    fun getFcmToken(): String{
        return preferences.getString(FCM_TOKEN,"").toString()
    }
} // End of SharedPreferencesUtil class