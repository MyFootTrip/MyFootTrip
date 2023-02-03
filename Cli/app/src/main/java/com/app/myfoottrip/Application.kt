package com.app.myfoottrip

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.app.myfoottrip.data.dao.VisitPlaceRepository
import com.app.myfoottrip.util.SharedPreferencesUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kakao.sdk.common.KakaoSdk
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "Application_싸피"

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        sharedPreferencesUtil = SharedPreferencesUtil(applicationContext)
        initRetrofit(AppInterceptor())
        initKakao()
        VisitPlaceRepository.initialize(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 서비스의 채널과 이름이 같아야함
            val channel = NotificationChannel(
                "location",
                "Location",
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    } // End of onCreate

    private fun initRetrofit(interceptor: AppInterceptor) {
        val logging = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            logging.level = HttpLoggingInterceptor.Level.BODY
        } else {
            logging.level = HttpLoggingInterceptor.Level.NONE
        }

        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addNetworkInterceptor(HttpLoggingInterceptor())
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(1, TimeUnit.MINUTES)
            .build()

        val gson: Gson = GsonBuilder() //날짜 데이터 포맷
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()

        val headerOkHttpClient = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addNetworkInterceptor(HttpLoggingInterceptor())
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(1, TimeUnit.MINUTES)
            .addInterceptor(interceptor)
            .build()

        headerRetrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(headerOkHttpClient)
            .build()

    } // End of initRetrofit

    inner class AppInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response = with(chain) {
            val newRequest = request().newBuilder()
                .addHeader("Authorization", sharedPreferencesUtil.getUserAccessToken())
                .build()
            proceed(newRequest)
        }
    } // End of AppInterceptor inner class

    private fun initKakao() {
        KakaoSdk.init(this, BuildConfig.KAKAO_APP_KEY)
    } // End of initKakao

    companion object {
        lateinit var retrofit: Retrofit
        lateinit var headerRetrofit: Retrofit

        const val SERVER_URL = "https://i8d103.p.ssafy.io/"    // TODO : AWS Hosting + URL 변경 //54.248.64.154
        // const val SERVER_URL = "http://i8d103.p.ssafy.io:7777/"

        const val IMG_URL = "http://54.248.64.154"
        lateinit var sharedPreferencesUtil: SharedPreferencesUtil
    }
}
