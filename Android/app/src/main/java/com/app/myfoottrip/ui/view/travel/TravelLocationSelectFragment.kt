package com.app.myfoottrip.ui.view.travel

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.PointF
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.model.VisitPlace
import com.app.myfoottrip.viewmodel.TravelActivityViewModel
import com.app.myfoottrip.viewmodel.TravelViewModel
import com.app.myfoottrip.databinding.FragmentTravelLocationSelectBinding
import com.app.myfoottrip.repository.VisitPlaceRepository
import com.app.myfoottrip.ui.adapter.CategoryAdatper
import com.app.myfoottrip.ui.view.main.HomeFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.LocationConstants
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import com.app.myfoottrip.util.showToastMessage
import com.google.android.material.chip.Chip
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.*
import java.util.*

private const val TAG = "TravelLocationSelectFragment_??????"

class TravelLocationSelectFragment : Fragment(), OnMapReadyCallback {
    // ViewModel
    private val travelViewModel by viewModels<TravelViewModel>()

    // ActivityViewModel
    private val travelActivityViewModel by activityViewModels<TravelActivityViewModel>()

    private lateinit var categoryAdapter: CategoryAdatper
    private var locationList: MutableList<String> = LinkedList() //?????? ?????????
    private var selectedList: MutableList<String> = LinkedList() //????????? ?????????

    private lateinit var naverMap: NaverMap //map??? ???????????? navermap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var mContext: Context
    lateinit var visitPlaceRepository: VisitPlaceRepository

    private var selectedTravelId = 0
    private lateinit var binding: FragmentTravelLocationSelectBinding

    // ????????? 0?????? ?????? ?????? ?????? ??????, ????????? 2?????? ????????? ?????? ????????? ????????????.
    private var fragmentType = 0

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
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitPlaceRepository = VisitPlaceRepository.get()
        checkAllPermission()
        // setupOnbackPressed()
    } // End of onCreate


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTravelLocationSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.visibility = View.VISIBLE
        binding.allConstrainlayout.visibility = View.GONE

        if (selectedList.isNotEmpty()) {
            binding.tvLocationHint.visibility = View.GONE
            binding.fabStart.apply {
                backgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.main)
                isEnabled = true
                isClickable = true
            }
        }

        // ????????? 0?????? ?????? ?????? ?????? ??????, ????????? 2?????? ????????? ?????? ????????? ????????????.
        fragmentType = requireArguments().getInt("type")
        getUserTravelDataResponseLiveDataObserve()

        // ???????????? ????????? ??????
        var temp: List<VisitPlace> = emptyList()
        CoroutineScope(Dispatchers.IO).launch {
            val removeDeffred: Deferred<Int> = async {
                val mainActivity = requireActivity() as MainActivity
                mainActivity.stopLocationBackground()
                try {
                    visitPlaceRepository.deleteAllVisitPlace()
                } catch (exception: Exception) {
                    Log.d(TAG, "onResume: DB??? ?????? ?????? ????????????.")
                }
                1
            }
            removeDeffred.await()


            val getDeffered: Deferred<Int> = async {
                temp = visitPlaceRepository.getAllVisitPlace()
                1
            }
            getDeffered.await()
        }


        binding.fabStart.apply {
            backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.gray_500)
            isEnabled = false
        }

        if (fragmentType == 2) {
            selectedTravelId = requireArguments().getInt("travelId")

            if (travelViewModel.getUserTravelDataResponseLiveData.value == null) {
                getUserTravelData()
            }

            // ????????? ???????????? ??? ?????????????????? ???????????? ??????, UI??? ????????????
        } else {
            // ????????? Travel??? ????????? ???
            // Adapter ?????????
            initAdapter()

            // EventListener ?????????
            initListener()
        }

        binding.progressBar.visibility = View.GONE
        binding.allConstrainlayout.visibility = View.VISIBLE
    } // End of onViewCreated

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    } // End of onDetach

    // Room DB??? ??????
    private fun saveRoomDB() = CoroutineScope(Dispatchers.IO).launch {
        val size = travelActivityViewModel.userTravelData.value!!.placeList!!.size
        for (i in 0 until size) {
            val place = travelActivityViewModel.userTravelData.value!!.placeList!![i]

            // place DTO??? placeImgList, ArrayList<String>??? List<Uri>??? ???????????? ?????????
            val temp = VisitPlace(
                i,
                place.address!!,
                place.placeId,
                place.latitude!!,
                place.longitude!!,
                place.saveDate!!.time,
                place.placeImgList!!,
                place.memo,
                place.placeName
            )
            visitPlaceRepository.insertVisitPlace(temp)
        }
    } // End of saveRoomDB

    private fun categoryAdapterEventListener() {
        if (selectedList.isNotEmpty()) {
            binding.tvLocationHint.visibility = View.GONE
            binding.fabStart.apply {
                backgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.main)
                isEnabled = true
                isClickable = true
            }
        }

        // categoryAdapter?????? ????????? ???????????? ?????? ???????????????
        categoryAdapter.setItemClickListener(object : CategoryAdatper.ItemClickListener {
            override fun onClick(view: View, position: Int, category: String) {
                if (!selectedList.contains(locationList[position]) && selectedList.size < 3) {
                    setChipListener(position)
                }

                // ???????????? ?????? ?????? ?????? ???????????? ?????? ?????? ?????? ???
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
    } // End of categoryAdapterEventListener

    private fun initAdapter() {
        locationList = LinkedList()
        locationList.addAll(HomeFragment.LOCATION_LIST)
        categoryAdapter = CategoryAdatper(locationList)

        binding.rvCategory.apply {
            adapter = categoryAdapter
        }

        categoryAdapterEventListener()
    } // End of initAdapter

    // ?????? ?????? ??????
    private fun startLocationRecording() {
        binding.fabStart.setOnClickListener {
            travelActivityViewModel.setLocationList(selectedList as LinkedList<String>)
            mContext.showToastMessage("?????? ????????? ???????????????.")

//            val mainActivity = requireActivity() as MainActivity
//            CoroutineScope(Dispatchers.IO).launch {
//                mainActivity.startLocationBackground()
//            }

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
                findNavController().popBackStack()
            }

            startLocationRecording()
        }
    } // End of initListener


    private fun setChipListener(position: Int) {
        if (!selectedList.contains(locationList[position]) && selectedList.size < 3) {
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

                // closeIcon ????????? ?????????
                setOnCloseIconClickListener {
                    binding.cgDetail.removeView(this)
                    // element??? ???????????? ??????
                    selectedList.remove(locationList[position])
                }
            })
        } else if (selectedList.isNotEmpty() && selectedList.size < 3) {
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

                // closeIcon ????????? ?????????
                setOnCloseIconClickListener {
                    binding.cgDetail.removeView(this)
                    // element??? ???????????? ??????
                    selectedList.remove(locationList[position])
                }
            })
        }
    } // End of setChipListener

    // ========================================================= ?????? ????????? ???????????? =========================================================
    private fun getUserTravelData() {
        CoroutineScope(Dispatchers.IO).launch {
            travelViewModel.getUserTravelData(selectedTravelId)
        }
    } // End of getUserTravelData

    private fun getUserTravelDataResponseLiveDataObserve() {
        travelViewModel.getUserTravelDataResponseLiveData.observe(this.viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    travelActivityViewModel.setUserTravelData(it.data!!)
                    locationList = LinkedList()

                    // ??????????????? ???????????? RoomDB??? ??????.
                    CoroutineScope(Dispatchers.IO).launch {
                        val deffered: Deferred<Int> = async {
                            saveRoomDB()
                            1
                        }
                        deffered.await()
                    }

                    selectedList = LinkedList()
                    selectedList.addAll(travelActivityViewModel.userTravelData.value!!.location)

                    // Adapter ?????????
                    initAdapter()
                    // EventListener ?????????
                    initListener()

                    if (selectedList.isNotEmpty() && selectedList.size < 3) {
                        for (i in 0 until selectedList.size) {
                            val idx = locationList.indexOf(selectedList[i])
                            setChipListener(idx)
                        }
                    }
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "?????? ????????? ???????????? ??????")
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "getUserTravelDataResponseLiveDataObserve: ?????? ???")
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


    // ?????? ????????? ?????? ??? ????????? ??????
    lateinit var getGPSPermissionLauncher: ActivityResultLauncher<Intent>

    private fun checkAllPermission() {
        // 1. GPS??? ?????? ???????????? ??????
        if (!isLocationServicesAvailable()) {
            // GPS??? ???????????? ????????? dialog?????? ????????? ???????????? ??? ??? ?????? ??????????????? ?????? ??????
            showDialogForLocationServiceSetting()
        } else {
            // GPS??? ???????????? ?????? ?????? ????????? ????????? ???????????? ??????
            isRunTimePermissionsGranted()
        }
    } // End of checkAllPermission

    private fun showDialogForLocationServiceSetting() {
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (isLocationServicesAvailable()) {
                    isRunTimePermissionsGranted()
                } else {
                    requireView().showSnackBarMessage("?????? ???????????? ????????? ??? ????????????.")
                    findNavController().popBackStack()
                }
            }
        }


        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext() as MainActivity)
        builder.run {
            setTitle("?????? ????????? ????????????")
            setMessage("?????? ???????????? ?????? ????????????, ?????? ??? ?????????????????????.")
            setCancelable(true)

            // ?????? ?????? ??????
            setPositiveButton("??????", DialogInterface.OnClickListener { dialog, id ->
                DialogInterface.OnClickListener { dialog, id ->
                    val callGPSSettingIntent = Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    )
                    getGPSPermissionLauncher.launch(callGPSSettingIntent)
                }
            })

            // ?????? ?????? ??????
            setNegativeButton("??????", DialogInterface.OnClickListener { dialog, id ->
                dialog.cancel()
                context.showToastMessage("???????????? ?????????????????? ?????? ??? ??????????????????")
                findNavController().popBackStack()
            })
            create().show() // ??????????????? ??????
        }
    } // End of showDialogForLocationServiceSetting

    private fun isLocationServicesAvailable(): Boolean {
        val locationManager = mContext.getSystemService(LOCATION_SERVICE) as LocationManager

        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                ))
    } // End of isLocationServicesAvailable


    private fun isRunTimePermissionsGranted() {
        // ?????? ????????? ????????? ??????
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            mContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            mContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            // ????????? ???????????? ?????? ?????? ????????? ??????????????? ??????
            ActivityCompat.requestPermissions(
                requireContext() as MainActivity,
                REQUIRED_PERMISSIONS,
                PERMISSION_REQUEST_CODE
            )
        }
    } // End of isRunTimePermissionsGranted

    private companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000

        // ?????? ??????
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // ????????? ?????? ????????? ????????? ?????? ??????
        const val PERMISSION_REQUEST_CODE = 100
    }
} // End of TravelLocationSelectFragment class
