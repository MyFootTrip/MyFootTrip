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
import androidx.core.os.bundleOf
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
import com.app.myfoottrip.util.LocationProvider
import com.app.myfoottrip.util.TimeUtils
import com.app.myfoottrip.util.showSnackBarMessage
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
import java.io.IOException
import java.util.*

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

    private var preCoor: Coordinates? = null
    private lateinit var logReceiver: LogReceiver

    inner class LogReceiver() : BroadcastReceiver() {
        var distCount = 0
        var flag = false

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            if ("test" == action) {
                val newCoor = intent.getSerializableExtra("test") as Coordinates
                Log.d(TAG, "onReceive: $newCoor")

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

                    if (dist > 500) {
                        // 가장 최근에 찍힌 좌표와 현재 나의 위치를 기준으로 500M를 벗어나면 다시 flag와 distCount를 초기화
                        flag = false
                        distCount = 0
                    }

                    if (dist <= 500) {
                        Log.d(TAG, "같은 위치")
                        Log.d(TAG, "dist calc: 오차 범위를 벗어나지 못함")

                        if (flag == false) {
                            distCount++
                        }

                        if (distCount == 4 && flag == false) {
                            // 지도에 마커표시 하기 위해서 DB등록
                            // 4번의 동일한 좌표가 찍히고 나면 RoomDB에 데이터 추가

                            CoroutineScope(Dispatchers.IO).launch {
                                var address: String? = null
                                val job = CoroutineScope(Dispatchers.IO).launch {
                                    val getAdd = getAddressByCoordinates(
                                        newCoor.latitude!!, newCoor.longitude!!
                                    )
                                    if (getAdd != null) {
                                        address = getAddressByCoordinates(
                                            newCoor.latitude!!, newCoor.longitude!!
                                        )!!.getAddressLine(0)
                                    }
                                }

                                job.join()

                                // 없는 주소는 List에서 생성하지 않고 빈 주소로 넣음
                                if (address == null) {
                                    address = "정확한 주소를 찾지 못했습니다 수정 작업에서 등록해주세요!"
                                }

                                saveTravel(newCoor.latitude!!, newCoor.longitude!!, address!!)
                            }

                            Log.d(TAG, "onReceive: 더 이상 count가 증가하지 않음 ${distCount}")
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        Log.d(TAG, "onAttach: TravelLocationWriteFragment 켜짐 ")
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitPlaceRepository = VisitPlaceRepository.get()
        locationProvider = LocationProvider(requireContext() as MainActivity)
        logReceiver = LogReceiver()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        activity!!.registerReceiver(logReceiver, IntentFilter("test"))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity!!.unregisterReceiver(logReceiver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentType = requireArguments().getInt("type")

        if (fragmentType == 2) {
            Log.d(TAG, "onViewCreated: 수정 작업 입니다.")
            Log.d(TAG, "가져온 데이터 : ${travelActivityViewModel.userTravelData.value}")

            // 가져온 데이터가 제대로 확인되면, RoomDB에 저장된 값 가져오기
            CoroutineScope(Dispatchers.IO).launch {
                getUserTravelData = visitPlaceRepository.getAllVisitPlace()
            }
        }

        changeMode(true)
        initMap()

        // 처음 시작 하자마자 좌표를 바로 들고옴
        locationClientSet()

        binding.tvStartTime.text = TimeUtils.getDateTimeString(System.currentTimeMillis())
        CoroutineScope(Dispatchers.IO).launch {
            setButtonListener()
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
                showToast("위치 기록을 중지합니다", ToastType.SUCCESS)

                serviceScope.cancel()
            }


            // 좌표 백그라운드 다시 시작
            fabRestart.setOnClickListener {
                val mainActivity = requireActivity() as MainActivity
                CoroutineScope(Dispatchers.IO).launch {
                    mainActivity.startLocationBackground()
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

            val bundle = bundleOf("type" to fragmentType)

            // 수정하는 페이지로 이동
            Navigation.findNavController(binding.fabStop)
                .navigate(R.id.action_travelLocationWriteFragment_to_editSaveTravelFragment, bundle)
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
                val job = CoroutineScope(Dispatchers.Default).launch {
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
                        Log.e(TAG, "nowLocationSave: 같은 좌표가 없거나, 테이블이 비어있습니다.")
                    }
                }


                if (nowLat == 0.0 || nowLng == 0.0) {
                    // 제대로된 좌표가 들어오지 않을 경우, 저장하지 않음
                    val v: View = requireView()
                    v.showSnackBarMessage("정확한 좌표를 찾고있습니다! 다시 저장해주세요!")
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
        locationClient!!.getLocationUpdates(2000L).catch { exception ->
            exception.printStackTrace()
        }.onEach { location ->
            travelViewModel.setRecentCoor(Coordinates(location.latitude, location.longitude))
        }.launchIn(
            serviceScope
        )
    }

    private suspend fun saveTravel(lat: Double, lon: Double, address: String) {
        binding.fabStop.isClickable = false
        binding.btnAddPoint.isClickable = false

        val temp = VisitPlace(
            0, address, lat, lon, System.currentTimeMillis(), emptyList()
        )

        CoroutineScope(Dispatchers.IO).launch {
            val deffered: Deferred<Int> = async {
                visitPlaceRepository.insertVisitPlace(temp)
                1
            }

            deffered.await()

            var temp: List<VisitPlace> = emptyList()
            val deffered2: Deferred<Int> = async {
                temp = visitPlaceRepository.getAllVisitPlace()
                1
            }
            deffered2.await()

            binding.fabStop.isClickable = true
            binding.btnAddPoint.isClickable = true
        }

        withContext(Dispatchers.Main) {
            showToast("현재 위치 저장 성공")
        }

        Log.d(TAG, "saveTravel: 저장 성공끝")
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

    private suspend fun getAddressByCoordinates(latitude: Double, longitude: Double): Address? {
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

    override fun onDetach() {
        super.onDetach()
        val mainActivity = requireActivity() as MainActivity
        CoroutineScope(Dispatchers.IO).launch {
            mainActivity.stopLocationBackground()
        }
    }

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
