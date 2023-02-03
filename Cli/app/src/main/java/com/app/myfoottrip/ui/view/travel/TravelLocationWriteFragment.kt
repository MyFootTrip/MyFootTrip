package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dao.VisitPlaceRepository
import com.app.myfoottrip.data.dto.Coordinates
import com.app.myfoottrip.data.dto.VisitPlace
import com.app.myfoottrip.data.viewmodel.TravelActivityViewModel
import com.app.myfoottrip.data.viewmodel.TravelViewModel
import com.app.myfoottrip.databinding.FragmentTravelLocationWriteBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.LocationConstants
import com.app.myfoottrip.util.LocationProvider
import com.app.myfoottrip.util.TimeUtils
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TAG = "TravelLocationWriteFragment_싸피"

class TravelLocationWriteFragment : BaseFragment<FragmentTravelLocationWriteBinding>(
    FragmentTravelLocationWriteBinding::bind, R.layout.fragment_travel_location_write
), OnMapReadyCallback {
    // ViewModel
    private val travelViewModel by viewModels<TravelViewModel>()

    // ActivityViewModel
    private val travelActivityViewModel by activityViewModels<TravelActivityViewModel>()

    private lateinit var locationProvider: LocationProvider
    private var mapFragment: MapFragment = MapFragment()
    private lateinit var naverMap: NaverMap //map에 들어가는 navermap
    private lateinit var locationSource: FusedLocationSource
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private lateinit var visitPlaceRepository: VisitPlaceRepository
    private lateinit var mContext: Context
    private var locationClient: LocationClient? = null

    private val preCoor: Coordinates? = null

    private var serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        Log.d(TAG, "onAttach: TravelLocationWriteFragment 켜짐 ")
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitPlaceRepository = VisitPlaceRepository.get()
        locationProvider = LocationProvider(requireContext() as MainActivity)
    }

    private fun locationClientSet() {
        serviceScope = CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

        locationClient = DefaultLocationClient(
            mContext, LocationServices.getFusedLocationProviderClient(mContext)
        )

        // 15분 기준으로 측정
        locationClient!!.getLocationUpdates(1000L * 60L * 15L).catch { exception ->
            exception.printStackTrace()
        }.onEach { location ->

            withContext(Dispatchers.IO) {
                travelViewModel.setRecentCoor(Coordinates(location.latitude, location.longitude))
            }

        }.launchIn(
            serviceScope
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()

        recentCoordinatesLiveDataObserve()

        // 처음 시작 하자마자 좌표를 바로 들고옴
        locationClientSet()
    } // End of onViewCreated

    private fun initialize() {
        changeMode(true)
        initMap()
        setButtonListener()
        initData()
    } // End of initialize

    private fun initData() {
        binding.tvStartTime.text = TimeUtils.getDateTimeString(System.currentTimeMillis())
        if (travelViewModel.selectedtravel == null) {
            //TODO : 기존 여정 세팅
        }
    } // End of initData


    private fun setButtonListener() {
        binding.apply {
            fabPause.setOnClickListener { //일시정지
                // LocationConstants.stopLocation()
                changeMode(false)

                val mainActivity = requireActivity() as MainActivity
                CoroutineScope(Dispatchers.IO).launch {
                    mainActivity.stopLocationBackground()
                }
                showToast("위치 기록을 중지합니다", ToastType.SUCCESS)

                serviceScope.cancel()
            }

            fabRestart.setOnClickListener {
                val mainActivity = requireActivity() as MainActivity
                CoroutineScope(Dispatchers.IO).launch {
                    mainActivity.stopLocationBackground()
                }
                showToast("위치 기록을 시작합니다.", ToastType.SUCCESS)
                changeMode(true)
                locationClientSet()
            }
        }

        // 위치기록
        // 정지 및 저장
        stopLocationRecordingAndSave()

        // 현재 위치 저장
        nowLocationSave()
    } // End of setButtonListener

    //위치 기록 정지 및 저장
    private fun stopLocationRecordingAndSave() {
        // 저장 버튼 클릭시 이벤트
        binding.fabStop.setOnClickListener {
            showToast("성공적으로 저장했습니다.", ToastType.SUCCESS)

            // 서비스 중지
            val mainActivity = requireActivity() as MainActivity
            mainActivity.stopLocationBackground()

            // 수정하는 페이지로 이동
            Navigation.findNavController(binding.fabStop)
                .navigate(R.id.action_travelLocationWriteFragment_to_editSaveTravelFragment)
        }
    } // End of stopLocationRecordingAndSaving

    // 현재 위치를 저장하는 메소드
    private fun nowLocationSave() {
        binding.btnAddPoint.setOnClickListener {
            saveTravel(
                locationProvider.getLocationLatitude(),
                locationProvider.getLocationLongitude()
            )
        }
    } // End of nowLocationSaving

    private fun saveTravel(lat: Double, lon: Double) {
        val temp = VisitPlace(
            0,
            "",
            lat,
            lon,
            System.currentTimeMillis(),
            emptyList()
        )

        CoroutineScope(Dispatchers.IO).launch {
            visitPlaceRepository.insertVisitPlace(temp)
        }

        showToast("현재 위치 저장 성공")
    } // End of saveTravel


    private fun initMap() {
        // TouchFrameLayout 에 mapFragment 올려놓기
        val fragmentTransaction = childFragmentManager.beginTransaction()
        if (mapFragment.isAdded) {
            fragmentTransaction.remove(mapFragment)
            mapFragment = MapFragment()
        }
        fragmentTransaction.add(R.id.map_view, mapFragment).commit()
        mapFragment.getMapAsync(this)
    } // End of initMap


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
    } // End of changeMode

    private fun recentCoordinatesLiveDataObserve() {
        travelViewModel.recentCoor.observe(viewLifecycleOwner) {
            // 좌표에 새로운 값이 들어왔는데, 오차 범위를 벗어나는지 계산해야됨.

            if (preCoor != null) {
                val dist = DistanceManager.getDistance(
                    it.latitude!!,
                    it.longitude!!,
                    preCoor.latitude!!,
                    preCoor.longitude!!
                )
                preCoor.latitude = it.latitude
                preCoor.longitude = it.longitude

                // 가장 최근에 찍힌 좌표와 현재 나의 위치를 기준으로 500M를 벗어나면 데이터를 저장함
                if (dist > 500) {
                    saveTravel(it.latitude!!, it.longitude!!)
                    Log.d(TAG, "dist calc: 오차 범위를 벗어나지 못함")
                }
            }
        }
    } // End of recentCoordinatesLiveDataObserve
    
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

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = true

        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource
    } // End of onMapReady

} // End of TravelLocationWriteFragment class