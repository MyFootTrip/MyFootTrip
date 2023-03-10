package com.app.myfoottrip.ui.view.travel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.model.Coordinates
import com.app.myfoottrip.model.VisitPlace
import com.app.myfoottrip.viewmodel.TravelActivityViewModel
import com.app.myfoottrip.viewmodel.TravelViewModel
import com.app.myfoottrip.databinding.FragmentTravelLocationWriteBinding
import com.app.myfoottrip.repository.VisitPlaceRepository
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.LocationProvider
import com.app.myfoottrip.util.TimeUtils
import com.app.myfoottrip.util.showSnackBarMessage
import com.app.myfoottrip.util.showToastMessage
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.IOException
import java.util.*

private const val TAG = "TravelLocationWriteFragment_싸피"

class TravelLocationWriteFragment : BaseFragment<FragmentTravelLocationWriteBinding>(
    FragmentTravelLocationWriteBinding::inflate
), OnMapReadyCallback { // End of TravelLocationWrite class
    // End of TravelLocationWriteFragment class
    // ViewModel
    private val travelViewModel by viewModels<TravelViewModel>()

    // ActivityViewModel
    private val travelActivityViewModel by activityViewModels<TravelActivityViewModel>()

    // Context
    private lateinit var mContext: Context

    // Naver 지도
    private lateinit var locationProvider: LocationProvider
    lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap //map에 들어가는 navermap
    private lateinit var locationSource: FusedLocationSource
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000


    // 현재 나의 좌표 배열
    private var myCoordinatesList: MutableList<Coordinates> = LinkedList()

    // 마커 배열
    private var markers: MutableList<Marker> = LinkedList()

    // polyline
    private val polyline = PolylineOverlay()


    // RoomDB
    private lateinit var visitPlaceRepository: VisitPlaceRepository

    private var locationClient: LocationClient? = null
    private var preCoor: Coordinates? = null

    // Service
    private lateinit var logReceiver: LogReceiver

    inner class LogReceiver() : BroadcastReceiver() {
        var distCount = 0
        var flag = false

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            if ("test" == action) {
                val newCoor = intent.getSerializableExtra("test") as Coordinates
                Log.d(TAG, "onReceive: $newCoor")
                Log.d(TAG, "onReceive: $distCount")

                if (preCoor != null) {
                    val dist = DistanceManager.getDistance(
                        newCoor.latitude!!,
                        newCoor.longitude!!,
                        preCoor!!.latitude!!,
                        preCoor!!.longitude!!
                    )
                    // 이전의 좌표를 아래에 저장해서 비교함
                    preCoor!!.latitude = newCoor.latitude
                    preCoor!!.longitude = newCoor.longitude

                    // 거리는 30M를 기준으로 설정
                    if (dist > 30) {
                        // 가장 최근에 찍힌 좌표와 현재 나의 위치를 기준으로 500M를 벗어나면 다시 flag와 distCount를 초기화
                        flag = false
                        distCount = 0
                    }

                    if (dist <= 30) {
                        Log.d(TAG, "같은 위치")
                        Log.d(TAG, "dist calc: 오차 범위를 벗어나지 못함")

                        if (flag == false) {
                            distCount++
                        }

                        if (distCount == 3 && flag == false) {
                            // 지도에 마커표시 하기 위해서 DB등록
                            // 4번의 동일한 좌표가 찍히고 나면 RoomDB에 데이터 추가

                            CoroutineScope(Dispatchers.IO).launch {
                                var address: String? = null

                                val addressDeffer = CoroutineScope(Dispatchers.IO).async {
                                    val getAdd = getAddressByCoordinates(
                                        newCoor.latitude!!, newCoor.longitude!!
                                    )
                                    if (getAdd != null) {
                                        address = getAdd.getAddressLine(0).toString()
                                    }
                                }

                                addressDeffer.join()
                                Log.d(TAG, "주소가 어떻게 나올까?: $address")

                                // 해당 좌표를 지도에 표시
                                withContext(Dispatchers.Main) {
                                    requireActivity().runOnUiThread {
                                        clearMapInMark()
                                        setInMapMarker(
                                            LatLng(
                                                newCoor.latitude!!, newCoor.longitude!!
                                            )
                                        )
                                    }
                                }

                                Log.d(TAG, "주소가 어떻게 나올까?: $address")

                                // 없는 주소는 List에서 생성하지 않고 빈 주소로 넣음
                                if (address == null) {
                                    address = "정확한 주소를 찾지 못했습니다 수정 작업에서 등록해주세요!"
                                }
                                saveTravel(newCoor.latitude!!, newCoor.longitude!!, address!!)
                            }

                            Log.d(TAG, "onReceive: 더 이상 count가 증가하지 않음 $distCount")
                            // 한번 마커가 표시되면, 더 이상 마커가 찍히지 않도록 flag값을 true로 고정해놓음
                            // 만약 오차범위를 벗어나면 그때 다시 flag를 false처리함.
                            flag = true
                            distCount = 0
                        }
                    }
                } else {
                    preCoor = newCoor
                }
            }
        }
    }

    private var serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    // 타입이 0이면 여행 정보 새로 생성, 타입이 2이면 기존의 여행 정보를 불러오기.
    private var fragmentType = 0
    private var getUserTravelData: List<VisitPlace> = emptyList()

    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        Log.d(TAG, "onAttach: TravelLocationWriteFragment 켜짐 ")
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitPlaceRepository = VisitPlaceRepository.get()
        locationProvider = LocationProvider(requireContext() as MainActivity)
        logReceiver = LogReceiver()
    } // End of onCreate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        activity!!.registerReceiver(logReceiver, IntentFilter("test"))
        return super.onCreateView(inflater, container, savedInstanceState)
    } // End of onCreateView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentType = requireArguments().getInt("type")

        // 남아있는 데이터 확인
        var temp: List<VisitPlace> = emptyList()
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.VISIBLE
                binding.allConstrainlayout.visibility = View.GONE
            }

            val deffered2: Deferred<Int> = async {
                temp = visitPlaceRepository.getAllVisitPlace()
                1
            }
            deffered2.await()

            if (fragmentType == 2) {
                Log.d(TAG, "onViewCreated: 수정 작업 입니다.")
                Log.d(TAG, "가져온 데이터 : ${travelActivityViewModel.userTravelData.value}")

                // 가져온 데이터가 제대로 확인되면, RoomDB에 저장된 값 가져오기
                CoroutineScope(Dispatchers.IO).launch {
                    getUserTravelData = visitPlaceRepository.getAllVisitPlace()

                    myCoordinatesList = LinkedList()
                    val size = getUserTravelData.size
                    for (i in 0 until size) {
                        myCoordinatesList.add(
                            Coordinates(
                                getUserTravelData[i].lat,
                                getUserTravelData[i].lng
                            )
                        )
                    }
                }
            }

            withContext(Dispatchers.Main) {
                changeMode(true)
                initMap()
            }

            // 처음 시작 하자마자 좌표를 바로 들고옴
            // locationClientSet()

            binding.tvStartTime.text = TimeUtils.getDateTimeString(System.currentTimeMillis())
            // CoroutineScope(Dispatchers.IO).launch {
            setButtonListener()
            // }

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                binding.allConstrainlayout.visibility = View.VISIBLE
            }

            setInMapMarker()

            val mainActivity = requireActivity() as MainActivity
            CoroutineScope(Dispatchers.IO).launch {
                mainActivity.startLocationBackground()
            }
        }
    } // End of onViewCreated

    private suspend fun setButtonListener() {
        binding.apply {
            // 좌표 백그라운드 작업 중지
            fabPause.setOnClickListener {
                changeMode(false)

                val mainActivity = requireActivity() as MainActivity
                CoroutineScope(Dispatchers.IO).launch {
                    mainActivity.stopLocationBackground()
                }
                // showToast(, ToastType.SUCCESS)
                requireContext().showToastMessage("위치 기록을 중지합니다")
                // serviceScope.cancel()
            }

            // 좌표 백그라운드 다시 시작
            fabRestart.setOnClickListener {
                val mainActivity = requireActivity() as MainActivity
                CoroutineScope(Dispatchers.IO).launch {
                    mainActivity.startLocationBackground()
                }
                showToast("위치 기록을 시작합니다.", ToastType.SUCCESS)
                changeMode(true)
                // locationClientSet()
            }
        }

        // 위치기록 정지 및 저장
        stopLocationRecordingAndSave()

        // 현재 위치 저장
        nowLocationSave()
    } // End of setButtonListener

    //위치 기록 정지 및 저장
    private fun stopLocationRecordingAndSave() {
        // 저장 버튼 클릭시 이벤트
        binding.fabStop.setOnClickListener {
            // 저장할 데이터가 있는지 우선적으로 파악해야됨.

            var temp: List<VisitPlace> = emptyList()
            CoroutineScope(Dispatchers.IO).launch {
                val deffered2: Deferred<Int> = async {
                    temp = visitPlaceRepository.getAllVisitPlace()
                    1
                }

                deffered2.await()

                if (temp.isEmpty()) {
                    //  저장할 내용이 없으므로 다음페이지로 넘어갈 수 없음.
                    requireView().showSnackBarMessage("아직 저장된 좌표가 없어요! 좌표를 추가해주세요")
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    // 서비스 중지
                    val mainActivity = requireActivity() as MainActivity
                    mainActivity.stopLocationBackground()

                    showToast("성공적으로 저장했습니다.", ToastType.SUCCESS)
                    val bundle = bundleOf("type" to fragmentType)

                    // 수정하는 페이지로 이동
                    Navigation.findNavController(binding.fabStop).navigate(
                        R.id.action_travelLocationWriteFragment_to_editSaveTravelFragment,
                        bundle
                    )
                }
            }
        }
    } // End of stopLocationRecordingAndSaving

    // 현재 위치를 저장하는 메소드
    private suspend fun nowLocationSave() = CoroutineScope(Dispatchers.IO).launch {
        binding.btnAddPoint.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                // 가장 최근에 찍힌 좌표가 이전의 좌표와 같을 경우, 저장을 수행하지 않음.

                val nowLat = locationProvider.getLocationLatitude()
                val nowLng = locationProvider.getLocationLongitude()
                var address: String? = null
                val job = CoroutineScope(Dispatchers.Default).async {
                    val getAdd = getAddressByCoordinates(nowLat, nowLng)
                    if (getAdd != null) {
                        address = getAdd.getAddressLine(0)
                    }
                }

                job.join()

                // 없는 주소는 List에서 생성하지 않고 빈 주소로 넣음
                if (address.isNullOrEmpty()) {
                    address = "정확한 주소를 찾지 못했습니다 수정 작업에서 등록해주세요!"
                }

                var recentPlace: VisitPlace? = null
                coroutineScope {
                    try {
                        recentPlace = visitPlaceRepository.getMostRecentVisitPlace()
                    } catch (exception: Exception) {
                        Log.e(TAG, "nowLocationSave: ${exception.printStackTrace()}")
                        Log.e(TAG, "nowLocationSave: 현재 테이블이 비어있습니다.")
                    }
                }

                // 가장 최근에 찍힌 좌표에서 지정된 (오차범위) 거리를 벗어나지 못했을 경우, 더 이상 찍히지 않음


                Log.d(TAG, "recentPlace: $recentPlace")

                if (nowLat == 0.0 || nowLng == 0.0) {
                    // 제대로된 좌표가 들어오지 않을 경우, 저장하지 않음
                    requireView().showSnackBarMessage("정확한 좌표를 찾고있습니다! 다시 저장해주세요!")
                } else if (recentPlace != null && recentPlace!!.lat != nowLat && recentPlace!!.lng != nowLng) {
                    // 가장 최근의 좌표와 현재 찍은 좌표가 같을 경우 저장하지 않음
                    saveTravel(
                        nowLat, nowLng, address!!
                    )
                } else if (recentPlace == null) {
                    saveTravel(
                        nowLat, nowLng, address!!
                    )
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("이전의 좌표와 동일해서 저장하지 않습니다")
                    }
                }
            }
        }
    } // End of nowLocationSaving


    private fun locationClientSet() {
        serviceScope = CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

        locationClient = DefaultLocationClient(
            mContext, LocationServices.getFusedLocationProviderClient(mContext)
        )

        // 15분 기준으로 측정
        locationClient!!.getLocationUpdates(1500L).catch { exception ->
            exception.printStackTrace()
        }.onEach { location ->
            travelViewModel.setRecentCoor(Coordinates(location.latitude, location.longitude))
        }.launchIn(
            serviceScope
        )
    }

    private suspend fun saveTravel(lat: Double, lon: Double, address: String) {
        withContext(Dispatchers.Main) {
            // 좌표가 저장되는 동안에는 버튼이 눌리지 않도록 설정
            binding.fabStop.isClickable = false
            binding.btnAddPoint.isClickable = false
        }


        // 새로 생성되는 위치에서는 PlaceId를 null값으로 넣음
        val temp = VisitPlace(
            0, address, null, lat, lon, System.currentTimeMillis(), LinkedList()
        )

        CoroutineScope(Dispatchers.IO).launch {
            val deffered: Deferred<Int> = async {
                visitPlaceRepository.insertVisitPlace(temp)
                1
            }

            deffered.join()

            var temp: List<VisitPlace> = emptyList()
            val deffered2: Deferred<Int> = async {
                temp = visitPlaceRepository.getAllVisitPlace()
                1
            }
            deffered2.join()

//            binding.fabStop.isClickable = true
//            binding.btnAddPoint.isClickable = true

            val clearMapInMarkDeffer: Deferred<Int> = async {
                clearMapInMark()
                1
            }

            clearMapInMarkDeffer.join()

            val deffer3: Deferred<Int> = async {
                setInMapMarker()
                1
            }

            deffer3.join()

            withContext(Dispatchers.Main) {
                showToast("현재 위치 저장 성공")

                // 좌표가 저장되는 동안에는 버튼이 눌리지 않도록 설정
                binding.fabStop.isClickable = true
                binding.btnAddPoint.isClickable = true
            }
        }
    } // End of saveTravel


    private fun initMap() {
        // TouchFrameLayout 에 mapFragment 올려놓기
//        val fragmentTransaction = childFragmentManager.beginTransaction()
//        if (mapFragment.isAdded) {
//            fragmentTransaction.remove(mapFragment)
//            mapFragment = MapFragment()
//        }
//        fragmentTransaction.add(R.id.map_view, mapFragment).commit()

        mapView = binding.mapView
        mapView.getMapAsync(this)
        locationSource = FusedLocationSource(this, 1000)
    } // End of initMap

    //기록 중인지 일시정지 중인지 화면 전환하는 코드
    private fun changeMode(type: Boolean) { // true : 진행중 false : 일시정지
        binding.apply {
            if (type) {
                clBackground.background = requireContext().getDrawable(R.color.main)
//                tvStartTime.setTextColor(requireContext().getColor(R.color.white))
                tvStartTimeLabel.setTextColor(requireContext().getColor(R.color.white))
                fabPause.visibility = View.VISIBLE
                fabPauseTv.visibility = View.VISIBLE
                btnAddPoint.visibility = View.VISIBLE
                fabRestart.visibility = View.GONE
                fabRestartTv.visibility = View.GONE
                fabStop.visibility = View.GONE
                fabStopTv.visibility = View.GONE

            } else {
                clBackground.background = requireContext().getDrawable(R.color.white)
                tvStartTime.setTextColor(requireContext().getColor(R.color.black))
                tvStartTimeLabel.setTextColor(requireContext().getColor(R.color.black))
                fabPause.visibility = View.GONE
                fabPauseTv.visibility = View.GONE
                btnAddPoint.visibility = View.GONE
                fabRestart.visibility = View.VISIBLE
                fabRestartTv.visibility = View.VISIBLE
                fabStop.visibility = View.VISIBLE
                fabStopTv.visibility = View.VISIBLE
            }
        }
    } // End of changeMode

    private fun getAddressByCoordinates(latitude: Double, longitude: Double): Address? {
        val geocoder = Geocoder(mContext, Locale.KOREA)

        val addresses: List<Address>?

        addresses = try {
            geocoder.getFromLocation(latitude, longitude, 7)
        } catch (ioException: IOException) {
            binding.root.showSnackBarMessage("지오코더 서비스 사용불가")
            ioException.printStackTrace()
            return null
        } catch (illegalArgumentException: java.lang.IllegalArgumentException) {
            illegalArgumentException.printStackTrace()
            binding.root.showSnackBarMessage("잘못된 위도 경도 입니다.")
            return null
        }

        if (addresses == null || addresses.isEmpty()) {
            binding.root.showSnackBarMessage("주소가 발견되지 않았습니다.")
            return null
        }

        val address: Address = addresses[0]
        return address
    } // End of getAddressByCoordinates


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
        val mainActivity = requireActivity() as MainActivity
        CoroutineScope(Dispatchers.IO).launch {
            mainActivity.stopLocationBackground()
        }
    } // End of onDestroy

    override fun onDestroyView() {
        super.onDestroyView()
        activity!!.unregisterReceiver(logReceiver)
    } // End of onDestroyView

    override fun onDetach() {
        super.onDetach()
        val mainActivity = requireActivity() as MainActivity
        CoroutineScope(Dispatchers.IO).launch {
            mainActivity.stopLocationBackground()
        }
        callback.remove()
    } // End of onDetach

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

        val cameraUpdate = CameraUpdate.scrollTo(
            LatLng(
                locationProvider.getLocationLatitude(), locationProvider.getLocationLongitude()
            )
        ).animate(CameraAnimation.Fly, 1000)
        naverMap.moveCamera(cameraUpdate)
    } // End of onMapReady

    private fun clearMapInMark() = CoroutineScope(Dispatchers.Main).launch {
//        getUserTravelData.forEach { mark ->
//            markers += Marker().apply {
//                position = LatLng(mark.lat, mark.lng)
//            }
//        }

        markers.forEach { marker ->
            marker.map = null
        }
        polyline.map = null
    }.onJoin

    private fun setInMapMarker(Coor: LatLng) = CoroutineScope(Dispatchers.Main).launch {
        markers = mutableListOf()

        getUserTravelData.forEach {
            markers += Marker().apply {
                position = LatLng(it.lat, it.lng)
                icon = MarkerIcons.BLACK
                captionText = it.placeName.toString()
            }
        }

        markers += Marker().apply {
            position = LatLng(Coor.latitude, Coor.longitude)
            icon = MarkerIcons.BLACK
        }

        markers.forEach { marker ->
            marker.map = naverMap
            marker.isIconPerspectiveEnabled = true
        }

        val size = getUserTravelData.size
        if (size >= 2) {
            val tempList: MutableList<LatLng> = ArrayList()
            for (i in 0 until size) {
                tempList.add(
                    LatLng(
                        getUserTravelData[i].lat, getUserTravelData[i].lng
                    )
                )
            }

            polyline.setPattern(10, 5)
            polyline.coords = tempList
            polyline.map = naverMap
        }
    }.onJoin // End of setMapInMark


    private fun setInMapMarker() = CoroutineScope(Dispatchers.Main).launch {
        markers = mutableListOf()

        var temp: List<VisitPlace> = emptyList()
        val deffered2: Deferred<Int> = async {
            withContext(Dispatchers.IO) {
                temp = visitPlaceRepository.getAllVisitPlace()
            }
            1
        }

        deffered2.join()

        temp.forEach {
            markers += Marker().apply {
                position = LatLng(it.lat, it.lng)
                icon = MarkerIcons.BLACK
                captionText = it.placeName.toString()
            }
        }


        markers.forEach { marker ->
            marker.map = naverMap
            marker.isIconPerspectiveEnabled = true
        }

        val size = getUserTravelData.size
        if (size >= 2) {
            // polyline = PolylineOverlay()
            val tempList: MutableList<LatLng> = ArrayList()
            for (i in 0 until size) {
                tempList.add(
                    LatLng(
                        getUserTravelData[i].lat, getUserTravelData[i].lng
                    )
                )
            }

            polyline.setPattern(10, 5)
            polyline.coords = tempList
            polyline.map = naverMap
        }
    }.onJoin // End of setMapInMark
} // End of TravelLocationWriteFragment class