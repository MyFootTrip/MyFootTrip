package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Location
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.repository.AppDatabase
import com.app.myfoottrip.data.viewmodel.TravelViewModel
import com.app.myfoottrip.databinding.FragmentTravelLocationWriteBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.LocationConstants
import com.app.myfoottrip.util.LocationProvider
import com.app.myfoottrip.util.TimeUtils
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "TravelLocationWriteFrag_싸피"

class TravelLocationWriteFragment : BaseFragment<FragmentTravelLocationWriteBinding>(
    FragmentTravelLocationWriteBinding::bind, R.layout.fragment_travel_location_write
), OnMapReadyCallback {

    lateinit var locationProvider: LocationProvider
    private val travelViewModel by activityViewModels<TravelViewModel>()
    private var mapFragment: MapFragment = MapFragment()
    private lateinit var naverMap: NaverMap //map에 들어가는 navermap
    private lateinit var locationSource: FusedLocationSource
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    val channelId = "com.app.myfoottrip"
    val channelName = "My Foot Trip channel"

    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()


        locationProvider = LocationProvider(requireContext() as MainActivity)
        updateUI()
    } // End of onViewCreated

    private fun initialize() {
        changeMode(true)
        LocationConstants.getLocationPermission {
            initMap()
        }
        
        setButtonListener()

        initData()
    } // End of initialize

    private fun initData() {
        binding.tvStartTime.text = TimeUtils.getDateTimeString(System.currentTimeMillis())
        if (travelViewModel.selectedtravel == null) {
            //TODO : 기존 여정 세팅

        }
    } // End of initData

    fun updateUI() {
        // 위도와 경도 정보를 가져오기
        val latitude: Double = locationProvider.getLocationLatitude()
        val longitude: Double = locationProvider.getLocationLongitude()

        Log.d(TAG, "updateUI: ${latitude}, ${longitude}")

        val marker = Marker()
        marker.position = LatLng(37.5670135, 126.9783740)
        // marker.map = requireActivity()
    } // End of updateUI


    private fun setButtonListener() {
        binding.apply {
            fabPause.setOnClickListener { //일시정지
                LocationConstants.stopLocation()
                changeMode(false)
            }

            fabRestart.setOnClickListener {
                LocationConstants.startBackgroundService(requireContext())
                showToast("위치 기록을 시작합니다.", ToastType.SUCCESS)
                changeMode(true)
            }
        }

        // 위치기록 정지 및 저장
        stopLocationRecordingAndSave()

        // 현재 위치 저장
        nowLocationSave()
    } // End of initListener

    //위치 기록 정지 및 저장
    private fun stopLocationRecordingAndSave() {
        binding.fabStop.setOnClickListener {
            saveData()
            showToast("성공적으로 저장했습니다.", ToastType.SUCCESS)
            //serviceStop()

            // 포어그라운드 중지
            val intent = Intent(mContext, LocationService::class.java)
            intent.action = LocationService.Actions.STOP_FOREGROUND
            val mainActivity = requireActivity() as MainActivity
            mainActivity.stopLocationService()


            // 수정하는 페이지로 이동
            Navigation.findNavController(binding.fabStop).navigate(R.id.action_travelLocationWriteFragment_to_editSaveTravelFragment)
            //findNavController().popBackStack()
        }
    } // End of stopLocationRecordingAndSaving
    
    private fun nowLocationSave() {
        binding.btnAddPoint.setOnClickListener {
            //LocationConstants.getNowLocation(requireContext())
            val latitude: Double = locationProvider.getLocationLatitude()
            val longitude: Double = locationProvider.getLocationLongitude()

            Log.d(TAG, "현재 위치 저장:  $latitude , $longitude")
        }
    } // End of nowLocationSaving


    private fun saveData() {
        //LocationConstants.stopLocation()
        CoroutineScope(Dispatchers.IO).launch {
            val list = AppDatabase.getInstance(requireContext()).locationDao().getAll()
            var placeList = arrayListOf<Place>()
            travelViewModel.makeTravel(
                Travel(
                    null, arrayListOf("서울"), Date(), Date(), placeList
                )
            )
        }
    } // End of saveData

    private fun initMap() {
        Log.d(TAG, "initMap: ")
        // TouchFrameLayout 에 mapFragment 올려놓기
        val fragmentTransaction = childFragmentManager.beginTransaction()
        if (mapFragment.isAdded) {
            fragmentTransaction.remove(mapFragment)
            mapFragment = MapFragment()
        }
        fragmentTransaction.add(R.id.map_view, mapFragment).commit()
        mapFragment.getMapAsync(this)
    } // End of initMap

    override fun onMapReady(naverMap: NaverMap) {
        Log.d(TAG, "onMapReady: ")
        this.naverMap = naverMap

        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = true

        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        setMarker(Marker(), 37.5970135, 126.9783740)
    } // End of onMapReady

    //기록 중인지 일시정지 중인지 화면 전환하는 코드
    private fun changeMode(type: Boolean) { // true : 진행중 false : 일시정지
        binding.apply {
            if (type) {
                clBackground.background = requireContext().getDrawable(R.color.main)
//                tvStartTime.setTextColor(requireContext().getColor(R.color.white))
                tvStartTimeLabel.setTextColor(requireContext().getColor(R.color.white))
                fabPause.visibility = View.VISIBLE
                btnAddPoint.visibility = View.VISIBLE
                fabRestart.visibility = View.GONE
                fabStop.visibility = View.GONE

            } else {
                clBackground.background = requireContext().getDrawable(R.color.white)
                tvStartTime.setTextColor(requireContext().getColor(R.color.black))
                tvStartTimeLabel.setTextColor(requireContext().getColor(R.color.black))
                fabPause.visibility = View.GONE
                btnAddPoint.visibility = View.GONE
                fabRestart.visibility = View.VISIBLE
                fabStop.visibility = View.VISIBLE
            }
        }
    }

    
    // 마커를 표시하는 메소드
    fun setMarker(marker: Marker, lat: Double, lng: Double) {
        marker.position = LatLng(lat, lng)
        marker.map = naverMap
        
    } // End of setMaker

    private fun makeTravelData() { //TODO : DB에서 값 가져와서 넣기
        CoroutineScope(Dispatchers.IO).launch {
            var placeList = arrayListOf<Place>()
            travelViewModel.makeTravel(
                Travel(
                    null, arrayListOf("서울"), Date(), Date(), placeList
                )
            )
        }
    }

    private fun locationToPlace(location: Location): Place {
        return Place(
            location.placeId,
            null,
            Date(location.time),
            null,
            null,
            location.lat,
            location.lng,
            location.address
        )
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    } // End of onStart

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    } // End of onResume

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    } // End of onPause

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    } // End of onStop

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        LocationConstants.serviceUnBind(requireContext())
    } // End of onDestroy

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    } // End of onLowMemory

} // End of TravelLocationWriteFragment class