package com.app.myfoottrip.util

import android.content.Context
import android.content.SharedPreferences
import com.app.myfoottrip.data.dto.Token

class SharedPreferencesUtil(context: Context) {

    var preferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    companion object {
        const val SHARED_PREFERENCES_NAME = "myfoottrip_preference"
        const val COOKIES_KEY_NAME = "cookies"
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
    }

    // 사용자의 토큰을 저장
    fun addUserToken(userToken: Token) {
        val editor = preferences.edit()
        editor.putString(ACCESS_TOKEN, userToken.access_token)
        editor.putString(REFRESH_TOKEN, userToken.refresh_token)
        editor.apply()
    } // End of addUserToken

    fun getUserToken(): Token {
        val accessToken = preferences.getString(ACCESS_TOKEN, "")
        val refreshToken = preferences.getString(REFRESH_TOKEN, "")

        if (refreshToken != "") {
            return Token(accessToken!!, refreshToken!!)
        } else {
            return Token("", "")
        }
    } // End of getUserToken

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

} // End of SharedPreferencesUtil class