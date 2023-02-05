package com.app.myfoottrip.ui.view.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.app.myfoottrip.data.viewmodel.TokenViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.ActivityMainBinding
import com.app.myfoottrip.ui.view.travel.LocationService
import com.app.myfoottrip.util.NetworkResult
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


private const val TAG = "MainActivity_마이풋트립"

class MainActivity : AppCompatActivity(), OnMapReadyCallback { // End of MainActivity class

    private lateinit var binding: ActivityMainBinding
    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    private val userViewModel: UserViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    lateinit var naverMap: NaverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 한번만 유저 정보를 가져오기
        getAccessTokenByRefreshTokenResponseLiveDataObserver()

        CoroutineScope(Dispatchers.IO).launch {
            getUserMyData()
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ), 0
        )

    } // End of onCreate

    private fun setBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    } // End of setBinding

    private suspend fun getUserMyData() {
        CoroutineScope(Dispatchers.IO).launch {
            tokenViewModel.getUserDataByAccessToken()
        }
    } // End of getUserMyData

    private fun getAccessTokenByRefreshTokenResponseLiveDataObserver() {
        tokenViewModel.getUserDataByAccessTokenResponseLiveData.observe(this) {

            when (it) {
                is NetworkResult.Success -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: ${it.data}")
                        
                        userViewModel.setWholeMyData(it.data!!)

                        coroutineScope {
                            if (it.data != null) {
                                setBinding()
                            }
                        }
                    }
                }

                is NetworkResult.Error -> {
                    // AccessToken을 통해서 유저 정보를 가져오기 실패했는지 파악해야됨.
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: 토큰 만료됨")
                    // RefreshToken을 통해서 AccessToken을 재발급
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: ${it.data}")
                    Log.d(
                        TAG,
                        "getAccessTokenByRefreshTokenResponseLiveDataObserver: ${it.message}"
                    )
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: 로딩 중입니다")
                }
            }
        }

    } // End of getAccessTokenByRefreshTokenResponseLiveDataObserver

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationBackground()
    }


    fun startLocationBackground() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }

        Log.d(TAG, "startLocationBackground: 동작함?")
    } // End of startLocationBackground

    fun stopLocationBackground() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            startService(this)
        }
    } // End of stopLocationBackground

//    private fun moveFragment(index : Int) {
//        val transaction = supportFragmentManager.beginTransaction()
//        when(index) {
//            1 -> {
//                transaction.replace(R.id.frame_layout_main, TravelSelectFragment())
//                    .addToBackStack(null)
//            }
//        }
//
//
//    } // End of moveFragment
}
