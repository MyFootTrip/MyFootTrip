package com.app.myfoottrip.ui.view.travel

import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.FragmentTravelLocationWriteBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.util.LocationConstants
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource

class TravelLocationWriteFragment : BaseFragment<FragmentTravelLocationWriteBinding>(
    FragmentTravelLocationWriteBinding::bind, R.layout.fragment_travel_location_write
), OnMapReadyCallback {

    private var mapFragment: MapFragment = MapFragment()
    private lateinit var naverMap: NaverMap //map에 들어가는 navermap

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize(){
        changeMode(true)
        LocationConstants.getLocationPermission {
            initMap()
        }
        initListener()
        initData()
    }

    private fun initData(){
        binding.tvStartTime.text = "04:20"


    }

    private fun initListener(){
        binding.apply {
            fabPause.setOnClickListener { //일시정지
                LocationConstants.stopLocation()
                changeMode(false)
            }
            fabStop.setOnClickListener{ //정지 및 저장
                //위치 기록 정지 및 저장
                LocationConstants.stopLocation()
                showToast("성공적으로 저장했습니다.",ToastType.SUCCESS)
            }
            fabRestart.setOnClickListener {
                LocationConstants.startBackgroundService(requireContext())
                showToast("위치 기록을 시작합니다.", ToastType.SUCCESS)
                changeMode(true)
            }
        }
    }

    private fun initMap(){
        Log.d(TAG, "initMap: ")
        // TouchFrameLayout 에 mapFragment 올려놓기
        val fragmentTransaction = childFragmentManager.beginTransaction()
        if(mapFragment.isAdded){
            fragmentTransaction.remove( mapFragment )
            mapFragment = MapFragment()
        }
        fragmentTransaction.add(R.id.map_view, mapFragment).commit()
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(p0: NaverMap) {
        Log.d(TAG, "onMapReady: ")
        this.naverMap = p0
    }


    private fun changeMode(type : Boolean){ // true : 진행중 false : 일시정지
        binding.apply {
            if(type){
                clBackground.background = requireContext().getDrawable(R.color.main)
                tvStartTime.setTextColor(requireContext().getColor(R.color.white))
                tvStartTimeLabel.setTextColor(requireContext().getColor(R.color.white))
                fabPause.visibility = View.VISIBLE
                fabRestart.visibility = View.GONE
                fabStop.visibility = View.GONE

            }else{
                clBackground.background = requireContext().getDrawable(R.color.white)
                tvStartTime.setTextColor(requireContext().getColor(R.color.black))
                tvStartTimeLabel.setTextColor(requireContext().getColor(R.color.black))
                fabPause.visibility = View.GONE
                fabRestart.visibility = View.VISIBLE
                fabStop.visibility = View.VISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        LocationConstants.serviceUnBind(requireContext())
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}