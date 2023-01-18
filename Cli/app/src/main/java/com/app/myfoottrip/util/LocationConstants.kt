package com.app.myfoottrip.util

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.app.myfoottrip.ui.view.travel.LocationService
import com.app.myfoottrip.ui.view.travel.TAG
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

object LocationConstants {
    val LOCATION_SERVICE_ID = 175

    private var isConService = false
    private var locationService : LocationService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val b = service as LocationService.MyServiceBinder
            locationService = b.getService()
            isConService = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isConService = false
        }
    }

    fun startBackgroundService(context: Context){
        getLocationPermission{
            if(!isConService){ //서비스가 연결되어 있지 않을 때
                serviceBind(context)
            }
            if(isConService && locationService != null){ //서비스가 있을 때
                locationService?.startLocationService()
            }else{
                Log.d(TAG, "callBindServiceTest: nulll======================= ")
            }
        }
    }

    fun getNowLocation(){
        getLocationPermission {

        }
    }

    fun getLocationPermission(callback : ()->Unit){//위치 권한 확인
        TedPermission.create().setPermissionListener(object : PermissionListener {
            //권한 허용
            override fun onPermissionGranted() {
                callback()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Log.d(TAG, "권한 요청 거부됨======================= ")
            }
        }).setDeniedMessage("위치 권한이 필요합니다.")
            .setPermissions(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .check()
    }

    fun stopLocation(){
        locationService?.stopLocationService()
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