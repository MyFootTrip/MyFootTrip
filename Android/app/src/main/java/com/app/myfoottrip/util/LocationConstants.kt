package com.app.myfoottrip.util

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.app.myfoottrip.ui.view.travel.LocationService
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

private const val TAG = "LocationConstants_싸피"
object LocationConstants {
    val LOCATION_SERVICE_ID = 175

    private var isConService = false
    private var locationService : LocationService? = null
    private var mFusedLocationClient : FusedLocationProviderClient? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            val b = service as LocationService.MyServiceBinder
//            locationService = b.getService()
//            isConService = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isConService = false
        }
    }

    fun startBackgroundService(context: Context){
//        getLocationPermission{
//            if(!isConService){ //서비스가 연결되어 있지 않을 때
//                serviceBind(context)
//            }
//            if(isConService && locationService != null){ //서비스가 있을 때
//                locationService?.setNotification()
//            }else{
//                Log.d(TAG, "callBindServiceTest: nulll======================= ")
//            }
//        }
    }

    fun getNowLocation(context: Context){
        var locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
            smallestDisplacement = 10.0f
        }
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)

        mFusedLocationClient = getFusedLocationProviderClient(context)
    }

    var locationCallback:LocationCallback = object :LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            val locationList = p0.locations
            if(locationList.size > 0){
                val location = locationList[locationList.size - 1]
                //TODO : 저장
                Log.d(TAG, "onLocationResult: ${location.latitude}, ${location.longitude}")
                if(mFusedLocationClient != null){
                    mFusedLocationClient!!.removeLocationUpdates(this)
                }
            }
        }
    }

    fun stopLocation(){
        //locationService?.stopLocationService()
    }

    fun serviceBind(context : Context){
        val intent = Intent(context, LocationService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun serviceUnBind(context : Context){
        if(isConService){
            context.unbindService(serviceConnection)
            isConService = false
        }
    }

}