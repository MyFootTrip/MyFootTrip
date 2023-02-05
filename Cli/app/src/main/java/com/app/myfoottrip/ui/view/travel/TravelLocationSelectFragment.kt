package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dao.VisitPlaceRepository
import com.app.myfoottrip.data.dto.VisitPlace
import com.app.myfoottrip.data.viewmodel.TravelActivityViewModel
import com.app.myfoottrip.data.viewmodel.TravelViewModel
import com.app.myfoottrip.databinding.FragmentTravelLocationSelectBinding
import com.app.myfoottrip.ui.adapter.CategoryAdatper
import com.app.myfoottrip.ui.view.main.HomeFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.LocationConstants
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showToastMessage
import com.google.android.material.chip.Chip
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.*

private const val TAG = "TravelLocationSelectFragment_싸피"

class TravelLocationSelectFragment : Fragment(), OnMapReadyCallback {
    // ViewModel
    private val travelViewModel by viewModels<TravelViewModel>()

    // ActivityViewModel
    private val travelActivityViewModel by activityViewModels<TravelActivityViewModel>()

    private lateinit var categoryAdapter: CategoryAdatper
    private var locationList: MutableList<String> = ArrayList() //지역 리스트
    private var selectedList: MutableList<String> = ArrayList() //선택된 리스트

    private var mapFragment: MapFragment = MapFragment()
    private lateinit var naverMap: NaverMap //map에 들어가는 navermap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var mContext: Context
    lateinit var visitPlaceRepository: VisitPlaceRepository

    private var selectedTravelId = 0
    private lateinit var binding: FragmentTravelLocationSelectBinding

    // 타입이 0이면 여행 정보 새로 생성, 타입이 2이면 기존의 여행 정보를 불러오기.
    private var fragmentType = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitPlaceRepository = VisitPlaceRepository.get()
    } // End of onCreate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTravelLocationSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 타입이 0이면 여행 정보 새로 생성, 타입이 2이면 기존의 여행 정보를 불러오기.
        fragmentType = requireArguments().getInt("type")

        // 다시 null 값으로 초기화
        getUserTravelDataResponseLiveDataObserve()

        binding.fabStart.apply {
            backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.gray_500)
            isEnabled = false
        }

//        LocationConstants.getLocationPermission {
//            initMap()
//        }

        if (fragmentType == 2) {
            selectedTravelId = requireArguments().getInt("travelId")
            Log.d(TAG, "onViewCreated: 수정 작업 입니다.")

            getUserTravelData()
            // 기존의 수정해야 할 유저데이터를 가져오고 나서, UI가 보여야됨
        } else {
            // Adapter 초기화
            initAdapter()

            // EventListener 초기화
            initListener()
        }
    } // End of onViewCreated

    private fun getUserTravelData() {
        CoroutineScope(Dispatchers.IO).launch {
            travelViewModel.getUserTravelData(selectedTravelId)
        }
    } // End of getUserTravelData

    private fun saveRoomDB() = CoroutineScope(Dispatchers.IO).launch {
        val size = travelActivityViewModel.userTravelData.value!!.placeList!!.size
        for (i in 0 until size) {
            val place = travelActivityViewModel.userTravelData.value!!.placeList!![i]

            val temp = VisitPlace(
                i,
                place.address!!,
                place.latitude!!,
                place.longitude!!,
                place.saveDate!!.time,
                place.placeImgList!!
            )
            visitPlaceRepository.insertVisitPlace(temp)
        }

    } // End of saveRoomDB

    private fun initMap() {
        // TouchFrameLayout에 mapFragment 올려놓기
        val fragmentTransaction = childFragmentManager.beginTransaction()
        if (mapFragment.isAdded) {
            fragmentTransaction.remove(mapFragment)
            mapFragment = MapFragment()
        }
        fragmentTransaction.add(R.id.map_fragment, mapFragment).commit()
        mapFragment.getMapAsync(this)
    } // End of initMap

    private fun initAdapter() {
        locationList.addAll(HomeFragment.LOCATION_LIST)
        categoryAdapter = CategoryAdatper(locationList)

        // categoryAdapter에서 아이템 클릭했을 경우 이벤트처리
        categoryAdapter.setItemClickListener(object : CategoryAdatper.ItemClickListener {
            override fun onClick(view: View, position: Int, category: String) {
                if (!selectedList.contains(locationList[position])) {
                    setChipListener(position)
                }
                if (selectedList.isNotEmpty()) {
                    binding.tvLocationHint.visibility = View.GONE
                    binding.fabStart.apply {
                        backgroundTintList =
                            AppCompatResources.getColorStateList(requireContext(), R.color.main)
                        isEnabled = true
                        isClickable = true
                    }
                }
            }
        })

        binding.rvCategory.apply {
            adapter = categoryAdapter
        }
    } // End of initAdapter

    // 위치 기록 시작
    private fun startLocationRecording() {
        binding.fabStart.setOnClickListener {
            travelActivityViewModel.setLocationList(selectedList as ArrayList<String>)

            val mainActivity = requireActivity() as MainActivity

            CoroutineScope(Dispatchers.IO).launch {
                mainActivity.startLocationBackground()
            }

            mContext.showToastMessage("위치 기록을 시작합니다.")

            val bundle = bundleOf(
                "type" to fragmentType
            )
            findNavController().navigate(
                R.id.action_travelLocationSelectFragment_to_travelLocationWriteFragment,
                bundle
            )
        }
    } // End of startLocationRecording

    private fun initListener() {
        binding.apply {
            ivLocationDrop.setOnClickListener {
                setRotaionAnimation()
            }
            tvLocationHint.setOnClickListener {
                setRotaionAnimation()
            }
            ivBack.setOnClickListener {
                travelViewModel
                findNavController().popBackStack()
            }
//            btnEnd.setOnClickListener{
//                LocationConstants.stopLocation()
//            }

            startLocationRecording()
        }
    } // End of initListener

    private fun setChipListener(position: Int) {
//        locationList = ArrayList() //지역 리스트
//        selectedList = ArrayList() //선택된 리스트
        selectedList.add(locationList[position])

        binding.cgDetail.addView(Chip(requireContext()).apply {
            chipCornerRadius = 10.0f
            text = locationList[position]
            textSize = 12.0f
            isCloseIconVisible = true
            closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
            closeIconTint =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
            chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.main))
            setTextColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )
            )

            // closeIcon 클릭시 이벤트
            setOnCloseIconClickListener {
                binding.cgDetail.removeView(this)
                // element를 기준으로 삭제
                selectedList.remove(locationList[position])

                if (selectedList.isEmpty()) {
                    binding.tvLocationHint.visibility = View.VISIBLE
                    binding.fabStart.apply {
                        backgroundTintList =
                            AppCompatResources.getColorStateList(requireContext(), R.color.gray_500)
                        isEnabled = false
                        isClickable = false
                    }
                }
            }
        })
    } // End of setChipListener

    private fun getUserTravelDataResponseLiveDataObserve() {
        travelViewModel.getUserTravelDataResponseLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    travelActivityViewModel.setGetUserTravelData(it.data!!)

                    Log.d(TAG, "getUserTravelDataResponseLiveDataObserve: 데이터 잘 가져와 지나?")
                    Log.d(TAG, "getUserTravelDataResponseLiveDataObserve: ${it.data}")
                    Log.d(TAG, "getUserTravelDataResponseLiveDataObserve: ${travelActivityViewModel.userTravelData.value}")

                    locationList = ArrayList()
                    selectedList = ArrayList()

                    // 수정해야할 데이터를 RoomDB에 저장.
                    CoroutineScope(Dispatchers.IO).launch {
                        val deffered: Deferred<Int> = async {
                            saveRoomDB()
                            1
                        }

                        deffered.await()
                    }

                    // Adapter 초기화
                    initAdapter()

                    // EventListener 초기화
                    initListener()

                    val size = travelActivityViewModel.userTravelData.value!!.location!!.size
                    for (i in 0 until size) {
                        setChipListener(locationList.indexOf(travelActivityViewModel.userTravelData.value!!.location!![i]))
                    }

                    if (selectedList.isNotEmpty()) {
                        binding.tvLocationHint.visibility = View.GONE
                        binding.fabStart.apply {
                            backgroundTintList =
                                AppCompatResources.getColorStateList(requireContext(), R.color.main)
                            isEnabled = true
                            isClickable = true
                        }
                    }
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "유저 데이터 가져오기 실패")
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "getUserTravelDataResponseLiveDataObserve: 로딩 중")
                }
            }
        }
    } // End of getUserTravelDataResponseLiveDataObserve


    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = true

        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        if (locationSource.lastLocation != null) {
            val cameraUpdate = CameraUpdate.scrollTo(
                LatLng(
                    locationSource.lastLocation!!.latitude, locationSource.lastLocation!!.longitude
                )
            )
            naverMap.moveCamera(cameraUpdate)
        }

        naverMap.onMapClickListener = object : NaverMap.OnMapClickListener {
            override fun onMapClick(p0: PointF, p1: LatLng) {
                if (binding.rvCategory.visibility == View.VISIBLE) {
                    binding.rvCategory.visibility = View.GONE
                }
            }
        }
    } // End of onMapReady

    private fun setRotaionAnimation() {
        binding.apply {
            if (rvCategory.visibility == View.VISIBLE) {
                rvCategory.visibility = View.GONE
                ivLocationDrop.animate().setDuration(200).rotation(0f)
            } else {
                rvCategory.visibility = View.VISIBLE
                ivLocationDrop.animate().setDuration(200).rotation(180f)
            }
        }
    } // End of setRotaionAnimation

    override fun onStart() {
        super.onStart()
        binding.mapFragment.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapFragment.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapFragment.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapFragment.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationConstants.serviceUnBind(requireContext())
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapFragment.onLowMemory()
    }

    private companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

} // End of TravelLocationSelectFragment class
