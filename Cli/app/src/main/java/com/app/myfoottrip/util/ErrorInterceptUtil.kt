package com.app.myfoottrip.util

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

private const val TAG = "ErrorInterceptUtil"

class ErrorInterceptUtil : Interceptor { // End of ErrorInterceptUtil

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        when (response.code) {
            500 -> {
                Log.d(TAG, "intercept: 서버 쪽 문제")
            }
            400 -> {
                Log.d(TAG, "intercept: 이쪽이 동작하나요?")
                Log.d(TAG, "intercept: ${response.body.toString()} ")
                Log.d(TAG, "intercept: ${response.headers}")
                Log.d(TAG, "intercept: ${response.message}")
                Log.d(TAG, "intercept: ${response.request.url}")
            }
        }

        return response
    } // End of intercept


} // End of ErrorInterceptUtil class