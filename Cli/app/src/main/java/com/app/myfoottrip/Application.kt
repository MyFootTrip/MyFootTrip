package com.app.myfoottrip

import android.app.Application
import android.content.SharedPreferences
import com.app.myfoottrip.util.SharedPreferencesUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        sharedPreferencesUtil = SharedPreferencesUtil(applicationContext)
        initRetrofit()
    }

    private fun initRetrofit() {
        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addNetworkInterceptor(HttpLoggingInterceptor())
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(1, TimeUnit.MINUTES).build()

        val gson : Gson = GsonBuilder() //날짜 데이터 포맷
            .setDateFormat("yyyy-mm-dd HH:mm:ss")
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    companion object {
        lateinit var retrofit: Retrofit
        const val SERVER_URL = "http://54.248.64.154/"    // TODO : AWS Hosting + URL 변경
        const val IMG_URL = "http://54.248.64.154"
        lateinit var sharedPreferencesUtil: SharedPreferencesUtil

    }

}