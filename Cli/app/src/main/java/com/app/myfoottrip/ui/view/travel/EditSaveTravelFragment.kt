package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dao.VisitPlaceRepository
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.viewmodel.TravelViewModel
import com.app.myfoottrip.databinding.FragmentEditSaveTravelBinding
import com.app.myfoottrip.ui.adapter.TravelEditSaveItemAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.start.JoinBackButtonCustomView
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "EditSaveTravelFragment_싸피"

class EditSaveTravelFragment : BaseFragment<FragmentEditSaveTravelBinding>(
    FragmentEditSaveTravelBinding::bind, R.layout.fragment_edit_save_travel
), OnMapReadyCallback { // End of EditSaveTravelFragment class
    lateinit var mapView: MapView
    private lateinit var mContext: Context
    lateinit var visitPlaceRepository: VisitPlaceRepository
    private val travelViewModel by activityViewModels<TravelViewModel>()
    private lateinit var joinBackButtonCustomView: JoinBackButtonCustomView

    private var userVisitPlaceDataList: List<Place> = emptyList()
    private var userTravelData: Travel? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var travelEditSaveItemAdapter: TravelEditSaveItemAdapter
    private var count = 0
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitPlaceRepository = VisitPlaceRepository.get()
    } // End of onCreate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapFragment

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        locationSource = FusedLocationSource(this, 1000)

        // 첫번째 UI에 데이터 뿌려야 하므로 데이터 부터 가져오기
        getData()

        createTravelResponseObserve()
    } // End of onViewCreated

    // 첫번째 UI에 데이터 뿌려야 하므로 데이터 부터 가져오기
    private fun getData() {
        // 데이터를 가져오고나서, Travel타입으로 변환한 후 UI로 뿌리는 작업을 진행한다.
        CoroutineScope(Dispatchers.IO).launch {
            userVisitPlaceDataList = getSqlLiteAllData()

            // Travel Dto로 변환
            coroutineScope {
                changeToTravelDto()
            }

            activity!!.runOnUiThread {
                CoroutineScope(Dispatchers.Main).launch {
                    setUI()
                    setMapInMark()
                    buttonEvents()
                }
            }
        }
    } // End of getData

    // 가져온 데이터로 지도에 좌표 마크 표시하기
    private fun setMapInMark() {
        val size = userVisitPlaceDataList.size
        for(i in 0 until size) {
            val marker = Marker()
            val data = userVisitPlaceDataList[i]

            marker.position = LatLng(data.latitude!!, data.longitude!!)
            marker.map = naverMap
            marker.icon = MarkerIcons.BLACK
            marker.iconTintColor = Color.RED
        }
    } // End of setMapInMark

    private fun changeToTravelDto() {
        userTravelData = Travel(
            null, travelViewModel.locationList, userVisitPlaceDataList[0].saveDate, // 처음 시작 저장 시간
            Date(System.currentTimeMillis()), // 마지막 저장 시간
            userVisitPlaceDataList
        )
    } // End of changeToTravelDto

    // 유저의 여행 데이터를 불러와서 UI를 뿌리기
    private suspend fun setUI() {
        joinBackButtonCustomView = binding.joinBackButtonCustomview
        binding.tvTravelTitle.text = userTravelData?.location?.joinToString(", ")

        val startDateFormat = SimpleDateFormat("yyyy-MM.dd", Locale("ko", "KR"))
        val endDateFormat = SimpleDateFormat("MM.dd", Locale("ko", "KR"))

        val startDateString = startDateFormat.format(userTravelData!!.startDate!!)
        val endDateString = endDateFormat.format(userTravelData!!.endDate!!)
        binding.travelDateTv.text = "$startDateString - $endDateString"

        binding.traveTotalTimeTv.text = "총 시간 : ${totalTimeCalc()} "

        recyclerView = binding.travelEditSaveRecyclerview
        initRecyclerViewAdapter()

        recyclerView.apply {
            adapter = travelEditSaveItemAdapter
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        }

        travelEditSaveItemAdapter.setItemClickListener(object :
            TravelEditSaveItemAdapter.ItemClickListener {
            override fun onEditButtonClick(position: Int, placeData: Place) {
                // 리사이클러뷰 포지션에 해당하는 수정 버튼을 눌렀을 때 이벤트
                showToast("${position + 1}의 아이템을 선택함")
            }
        })
    } // End of setUI

    private fun initRecyclerViewAdapter() {
        travelEditSaveItemAdapter = TravelEditSaveItemAdapter(mContext, userTravelData?.placeList!!)
    } // End of initRecyclerViewAdapter

    private fun totalTimeCalc(): String {
        val result = "00시간 00분"
        
        val startTime = userTravelData!!.startDate!!.time
        val endTime = userTravelData!!.endDate!!.time
        val diff = endTime - startTime

        val dateFormat = SimpleDateFormat("HH : mm", Locale("ko", "KR"))
        val temp = dateFormat.format(diff)

        Log.d(TAG, "totalTimeCalc: $temp ")

        /*
            출발 시간 에서 끝난 시간을 Long타입으로 계산해서 차이값을 시간으로 표현하면됨
         */
        return result
    } // End of totalTimeCalc

    private fun buttonEvents() {
        // 저장 버튼 눌렀을 때 이벤트
        binding.travelEditSaveButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                createTravel()
            }
        }

        joinBackButtonCustomView.findViewById<AppCompatButton>(R.id.custom_back_button_appcompatbutton)
            .setOnClickListener {
                findNavController().popBackStack()
            }
    } // End of buttonEvents

    private suspend fun createTravel() {
        CoroutineScope(Dispatchers.IO).launch {
            // 변환된 Travel데이터를 서버에 저장
            coroutineScope {
                userTravelData?.let { travelViewModel.createTravel(it) }
            }
        }
    } // End of createTravel

    private suspend fun getSqlLiteAllData(): List<Place> {
        val placeList: MutableList<Place> = ArrayList()

        val job = CoroutineScope(Dispatchers.IO).async {
            val allVisitPlaceList = visitPlaceRepository.getAllVisitPlace()

            // visitPlace를 변경해서 Place로 변경
            val size = allVisitPlaceList.size

            // SQLLite 전체 데이터 가져오기
            for (i in 0 until size) {
                val temp = allVisitPlaceList[i]

                var address = ""
                val job = CoroutineScope(Dispatchers.IO).launch {
                    address = getAddressByCoordinates(temp.lat, temp.lng)!!.getAddressLine(0)
                }

                job.join()

                placeList.add(
                    Place(
                        null, "", temp.date?.let { Date(it) }, "", // 일단 처음에는 메모 빈 값
                        ArrayList(), // 일단 빈 이미지를 넣어야됨
                        temp.lat, // 좌표
                        temp.lng, // 좌표
                        address
                    )
                )
            }
        }

        job.await()
        job.join()

        return placeList
    } // End of getSqlLiteAllData

    private fun createTravelResponseObserve() {
        travelViewModel.createTravelResponseLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    if (it.data == 201) {
                        // 성공하면, SQLLite table 모두 비우기
                        CoroutineScope(Dispatchers.IO).launch {
                            visitPlaceRepository.deleteAllVisitPlace()
                            Log.d(
                                TAG,
                                "현재 남아있는 SQLLite Data : ${visitPlaceRepository.getAllVisitPlace()}"
                            )

                            // DB를 비우고 빠져나가기
                            coroutineScope {
                                withContext(Dispatchers.Main) {

                                    // 다시 여행 선택페이지로 이동
                                    val bundle = bundleOf("type" to 0)
                                    findNavController().navigate(
                                        R.id.action_editSaveTravelFragment_to_travelSelectFragment,
                                        bundle
                                    )

                                }
                            }
                        }
                    }
                }

                is NetworkResult.Error -> {
                    binding.root.showSnackBarMessage("유저 여행 데이터 저장 오류 발생")
                    Log.d(TAG, "createTravelResponseLiveData Error: ${it.data}")
                    Log.d(TAG, "createTravelResponseLiveData Error: ${it.message}")
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "createTravelResponseLiveData Loading")
                }
            }
        }
    } // End of createTravelResponseObserve

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
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
    } // End of onMapReady
} // End of EditSaveTravelFragment
