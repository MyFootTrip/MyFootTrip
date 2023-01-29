package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.TravelViewModel
import com.app.myfoottrip.databinding.FragmentTravelLocationSelectBinding
import com.app.myfoottrip.ui.adapter.CategoryAdatper
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.HomeFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.LocationConstants
import com.google.android.material.chip.Chip
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource

private const val TAG = "TravelLocationSelectFragment_싸피"

class TravelLocationSelectFragment : BaseFragment<FragmentTravelLocationSelectBinding>(
    FragmentTravelLocationSelectBinding::bind, R.layout.fragment_travel_location_select
), OnMapReadyCallback {
    private val travelViewModel by activityViewModels<TravelViewModel>()
    private lateinit var categoryAdapter: CategoryAdatper
    private var locationList: ArrayList<String> = arrayListOf() //지역 리스트
    private var selectedList: ArrayList<String> = arrayListOf() //선택된 리스트

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private var mapFragment: MapFragment = MapFragment()
    private lateinit var naverMap: NaverMap //map에 들어가는 navermap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //서비스 연결
        LocationConstants.serviceBind(requireContext())

        binding.fabStart.apply {
            backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.gray_500)
            isEnabled = false
        }

        LocationConstants.getLocationPermission {
            initMap()
        }
        initAdapter()
        initListener()

        Log.d(TAG, "onViewCreated: ")
    } // End of onViewCreated


    private fun initMap() {
        // TouchFrameLayout 에 mapFragment 올려놓기
        val fragmentTransaction = childFragmentManager.beginTransaction()
        if (mapFragment.isAdded) {
            fragmentTransaction.remove(mapFragment)
            mapFragment = MapFragment()
        }
        fragmentTransaction.add(R.id.map_fragment, mapFragment).commit()
        mapFragment.getMapAsync(this)
    } // End of initMap

    private fun initAdapter() {
        locationList.clear()
        selectedList.clear()
        locationList.addAll(HomeFragment.LOCATION_LIST)
        categoryAdapter = CategoryAdatper(locationList)

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
            travelViewModel.setLocationList(selectedList)
//            val view = TravelLocationSelectFragment().view
//            if (view != null) {
//                serviceStart(view)
//            }

            val mainActivity = requireActivity() as MainActivity
            mainActivity.startLocationService()

            // LocationConstants.startBackgroundService(requireContext())
            showToast("위치 기록을 시작합니다.", ToastType.SUCCESS)
            findNavController().navigate(R.id.action_travelLocationSelectFragment_to_travelLocationWriteFragment)
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
                findNavController().popBackStack()
            }
//            btnEnd.setOnClickListener{
//                LocationConstants.stopLocation()
//            }

            startLocationRecording()
        }
    }

    private fun setChipListener(position: Int) {
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

            setOnCloseIconClickListener {
                binding.cgDetail.removeView(this)
                selectedList.remove(locationList[position])
                if (selectedList.isEmpty()) {
                    binding.tvLocationHint.visibility = View.VISIBLE
                    binding.fabStart.apply {
                        backgroundTintList =
                            AppCompatResources.getColorStateList(requireContext(), R.color.gray_500)
                        isEnabled = false
                    }
                }
            }
        })
    }

    fun setMarker(marker: Marker, lat: Double, lng: Double) {
        marker.isIconPerspectiveEnabled = true
        marker.icon = OverlayImage.fromResource(R.drawable.ic_marker_test)
        marker.alpha = 0.8f
        marker.position = LatLng(lat, lng)
        marker.map = naverMap
    } // End of setMaker

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
    }

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
    }

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

}