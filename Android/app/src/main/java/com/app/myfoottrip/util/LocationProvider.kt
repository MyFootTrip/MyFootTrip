package com.app.myfoottrip.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

class LocationProvider(val context : Context) {
    private var location: Location? = null
    private var locationManager: LocationManager? = null

    init {
        getLocation()
    }

    private fun getLocation(): Location? {
        try {
            // 위치 시스템 서비스를 가져오기
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            var gpsLocation: Location? = null
            var networkLocation: Location? = null

            // GPS Provider와 Network Provider가 활성화 되어있는지 확인
            val isGPSEnabled: Boolean = locationManager!!.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )

            val isNetworkEnabled: Boolean = locationManager!!.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )

            // 둘다 사용 불가능할 경우 null을 return
            if (!isGPSEnabled && !isNetworkEnabled) {
                return null
            } else {
                val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                    // COARSE 보다 정밀한 위치
                )

                val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    return null
                }


                // 네트워크를 사용가능 할 경우 네트워크를 통해서 위치 가져오기
                if (isNetworkEnabled) {
                    networkLocation = locationManager?.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER
                    )
                }

                // GPS를 통해서 위치 파악이 가능한 경우 위치를 가져오기
                if (isGPSEnabled) {
                    gpsLocation = locationManager?.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER
                    )
                }

                // 둘다 위치 정보를 가지고 있다면 둘 중 정확도가 더 높은것을 선택
                if (gpsLocation != null && networkLocation != null) {
                    if (gpsLocation.accuracy > networkLocation.accuracy) {
                        location = gpsLocation
                        return gpsLocation
                    } else {
                        location = networkLocation
                        return networkLocation
                    }
                } else {
                    // 가능한 위치 정보 하나만 사용
                    if (gpsLocation != null) {
                        location = gpsLocation
                    } else if (networkLocation != null) {
                        location = networkLocation
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return location
    } // End of getLocation

    fun getLocationLatitude(): Double {
        return location?.latitude ?: 0.0
    } // End of getLocationLatitude

    fun getLocationLongitude(): Double {
        return location?.longitude ?: 0.0
    } // End of getLocationLongitude

}