package com.app.myfoottrip.ui.view.travel

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dao.VisitPlaceRepository
import com.app.myfoottrip.data.dto.Coordinates
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*
import kotlin.concurrent.timer

private const val TAG = "CoordinatesService_싸피"

class LocationService : Service() {
    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )
    private lateinit var locationClient: LocationClient
    lateinit var visitPlaceRepository: VisitPlaceRepository

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext, LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        visitPlaceRepository = VisitPlaceRepository.get()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        Log.d(TAG, "locationService : start")
        
        
        val notification =
            NotificationCompat.Builder(this, "location").setContentTitle("마이풋트립")
                .setContentText("위치 정보를 수집 중").setSmallIcon(R.drawable.ic_notify_round_icon)
                .setOngoing(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

//        val timer = Timer()
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//
//                Log.d(TAG, "run: ")
//            }
//        }, (1000L * 60L * 1L), (1000L * 60L * 1L))

        // 15분마다 측정
        locationClient.getLocationUpdates(150_000L).catch { exception ->
            exception.printStackTrace()
        }.onEach { location ->
            val lat = location.latitude.toString()
            val lon = location.longitude.toString()
            val updateNotification = notification.setContentText(
                "위치를 측정 중.. $lat , $lon"
            )

            val intent = Intent("test")
            intent.putExtra("test", Coordinates(location.latitude, location.longitude))
            applicationContext.sendBroadcast(intent)



            CoroutineScope(Dispatchers.IO).launch {
                EventBus.post(Coordinates(location.latitude, location.longitude))
            }
            // notificationManager.notify(1, updateNotification.build())
        }
            .launchIn(
                serviceScope
            )

        startForeground(1, notification.build())
    } // End of start

    private fun stop() {
        stopForeground(true)
        stopSelf()
    } // End of stop

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
} // End of CoordinatesService class
