package com.app.myfoottrip.ui.view.travel

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Location
import com.app.myfoottrip.data.repository.AppDatabase
import com.app.myfoottrip.util.LocationConstants
import com.app.myfoottrip.util.TimeUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.NaverMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.abs

private const val TAG = "LocationService_싸피"

class LocationService : Service() {
    val binder = MyServiceBinder()
    var locationCount = 0
    private lateinit var naverMap: NaverMap
    val channelId = "com.app.myfoottrip"
    val channelName = "My Foot Trip channel"


    private var travelLocationWriteFragment: TravelLocationWriteFragment =
        TravelLocationWriteFragment()
    private var travelLocationSelectFragment: TravelLocationSelectFragment =
        TravelLocationSelectFragment()


    init {
        travelLocationWriteFragment = TravelLocationWriteFragment()
        travelLocationSelectFragment = TravelLocationSelectFragment()

    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind: DB 세팅 완료=====")
        return binder
    } // End of onBind


    private var mLocationCallback = object : LocationCallback() { //위치 받아서 저장하는 코드
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (locationResult != null && locationResult.lastLocation != null) {
                var latitude = locationResult.lastLocation.latitude
                var longitude = locationResult.lastLocation.longitude
                var time = System.currentTimeMillis()
                Log.d(
                    TAG,
                    "위도 : ${latitude}, 경도 : ${longitude}, 시간 : ${TimeUtils.getDateTimeString(time)}"
                )

                CoroutineScope(Dispatchers.IO).launch {
                    AppDatabase.getInstance(applicationContext).locationDao().apply {
                        val location: Location? = getLastOne()
                        if (location != null) {
                            var diff = abs(latitude - location.lat)
                            diff += abs(longitude - location.lng)
                            if (diff < 0.0005) {
                                Log.d(TAG, "onLocationResult: 동일한 거리")
                                return@apply
                            }
                        }
                        insertLocation(
                            Location(
                                null, null, latitude, longitude, time, "주소"
                            )
                        ) //TODO :
                        val count = getCount()
                        Log.d(TAG, "DB에 들어감 COUNT : $count")
                    }
                }
            }
        }
    } // End of mLocationCallback

    
    // 포어그라운드를 위한 notification 등록
    fun setNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            var notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(
            this, channelId
        ).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("My Foot Trip")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)

        val notification = notificationBuilder.build()
        val NOTIFICATION_ID = 1234
        startForeground(NOTIFICATION_ID, notification)

        getNowMyLocation(notificationBuilder)
    } // End of setNotification
    
    
    // 현재 위치 가져오기
    private fun getNowMyLocation(notificationBuilder: NotificationCompat.Builder) {
        val locationRequest = LocationRequest.create()
        locationRequest.apply { //위치 받아오는 interval 설정
//            setInterval(1_000 * 60 * 15) //15분
//            setFastestInterval(1_000 * 60 * 15) //15분
            // 테스트 용 30초
            setInterval(1_000 * 10) // 10초
            setFastestInterval(1_000 * 10) // 10초
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        }

        //위치 받아오기 시작
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper())
        startForeground(LocationConstants.LOCATION_SERVICE_ID, notificationBuilder.build())
    } // End of getNowMyLocation

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setNotification()

        val nc: Notification =
            NotificationCompat.Builder(this, channelId).setContentTitle("MyFootTrip")
                .setSmallIcon(R.mipmap.ic_launcher).build()
        startForeground(1, nc)

        when(intent?.action) {
            Actions.START_FOREGROUND -> {
                startForeground(1, nc)
            }
            Actions.STOP_FOREGROUND -> {
                stopLocationService()
            }
        }; return START_STICKY
    }




    fun stopLocationService() { //위치 받아오기 종료
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(mLocationCallback)
        stopForeground(true)
        stopSelf()
    } // End of stopLocationService

    inner class MyServiceBinder : Binder() {
        fun getService(): LocationService {
            return this@LocationService
        } // End of getService
    } // End of MyServiceBinder inner class

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    } // End of onDestroy

    object Actions {
        private const val prefix = "com.app.myfoottrip"
        const val START_FOREGROUND = prefix + "startforeground"
        const val STOP_FOREGROUND = prefix + "stopforeground"
        const val STOP = prefix + "stop"
    }

    fun startLocationService() { //위치 저장 시작
//        var notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        val resultIntent = Intent()
//        val pendingIntent =
//            PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE)
//        val builder = NotificationCompat.Builder(applicationContext, channelId)
//        builder.apply {
//            setSmallIcon(R.mipmap.ic_launcher)
//            setContentTitle("My Foot Trip")
//            setDefaults(NotificationCompat.DEFAULT_ALL)
//            setContentText("위치 기록중...")
//            setContentIntent(pendingIntent)
//            setAutoCancel(false)
//            setPriority(NotificationCompat.PRIORITY_MAX)
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
//                val notificationChannel = NotificationChannel(
//                    channelId, channelName, NotificationManager.IMPORTANCE_HIGH
//                )
//                notificationChannel.description = "This channel is used by location service"
//                notificationManager.createNotificationChannel(notificationChannel)
//            }
//        }



        //권한 확인
        val hasFineLocationPermission = ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoraseLocation = ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED && hasCoraseLocation != PackageManager.PERMISSION_GRANTED) {
            return
        }

    } // End of startLocationService

} // End of LocationService class
