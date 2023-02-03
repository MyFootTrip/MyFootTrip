package com.app.myfoottrip.ui.view.travel

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.app.myfoottrip.Application
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dao.VisitPlaceRepository
import com.app.myfoottrip.data.dto.Coordinates
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
        val notification =
            NotificationCompat.Builder(this, "location").setContentTitle("Tracking location...")
                .setContentText("Location: null").setSmallIcon(R.drawable.ic_launcher_background)
                .setOngoing(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient.getLocationUpdates(2000L).catch { exception ->
            exception.printStackTrace()
        }.onEach { location ->
            // 소수점 3자리 까지만 가져옴
            val lat = location.latitude.toString().takeLast(3)
            val lon = location.longitude.toString().takeLast(3)
            val updateNotification = notification.setContentText(
                "위치를 측정 중.. $lat , $lon"
            )

            CoroutineScope(Dispatchers.IO).launch {
                EventBus.post(Coordinates(location.latitude, location.longitude))
            }

            notificationManager.notify(1, updateNotification.build())
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
