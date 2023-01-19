package com.app.myfoottrip.ui.view.travel

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.app.myfoottrip.data.dto.Location
import com.app.myfoottrip.data.repository.AppDatabase
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.app.myfoottrip.R
import com.app.myfoottrip.util.LocationConstants
import com.app.myfoottrip.util.TimeUtils
import java.lang.Math.abs

const val TAG = "areum_Service"
class LocationService : Service() {
    val binder = MyServiceBinder()

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind: DB 세팅 완료=====")
        return binder
    }

    private var mLocationCallback = object : LocationCallback(){ //위치 받아서 저장하는 코드
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if(locationResult != null && locationResult.lastLocation != null){
                var latitude = locationResult.lastLocation.latitude
                var longitude = locationResult.lastLocation.longitude
                var time = System.currentTimeMillis()
                Log.d("areum", "위도 : ${latitude}, 경도 : ${longitude}, 시간 : ${TimeUtils.getDateTimeString(time)}")

                CoroutineScope(Dispatchers.IO).launch {
                    AppDatabase.getInstance(applicationContext).locationDao().apply {
                        val location : Location? = getLastOne()
                        if(location != null){
                            var diff = abs(latitude - location.lat)
                            diff += abs(longitude - location.lng)
                            if(diff < 0.0005){
                                Log.d(TAG, "onLocationResult: 동일한 거리")
                                return@apply
                            }
                        }
                        insertLocation( Location(null, null, latitude, longitude, time,"주소") ) //TODO :
                        val count = getCount()
                        Log.d(TAG, "DB에 들어감 COUNT : $count")
                    }
                }
            }
        }
    }



    fun startLocationService(){ //위치 저장 시작
        var channelId = "location_notification_channel"
        var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val resultIntent = Intent()
        val pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(applicationContext,channelId)
        builder.apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle("My Foot Trip")
            setDefaults(NotificationCompat.DEFAULT_ALL)
            setContentText("위치 기록중...")
            setContentIntent(pendingIntent)
            setAutoCancel(false)
            setPriority(NotificationCompat.PRIORITY_MAX)
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(notificationManager != null && notificationManager.getNotificationChannel(channelId) == null){
                val notificationChannel = NotificationChannel(channelId,"My Foot Trip", NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.description = "This channel is used by location service"
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        val locationRequest = LocationRequest.create()
        locationRequest.apply { //위치 받아오는 interval 설정
            setInterval(1_000 * 60 * 15) //15분
            setFastestInterval(1_000 * 60 * 15) //15분
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        }

        //권한 확인
        val hasFineLocationPermission = ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoraseLocation = ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED && hasCoraseLocation != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        //위치 받아오기 시작
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest,mLocationCallback, Looper.getMainLooper())
        startForeground(LocationConstants.LOCATION_SERVICE_ID, builder.build())
    }

    fun stopLocationService(){ //위치 받아오기 종료
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(mLocationCallback)
        stopForeground(true)
        stopSelf()
    }

    inner class MyServiceBinder : Binder(){
        fun getService() : LocationService{
            return this@LocationService
        }
    }
}