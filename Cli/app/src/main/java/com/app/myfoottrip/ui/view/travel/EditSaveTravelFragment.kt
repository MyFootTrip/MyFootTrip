package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dao.VisitPlaceRepository
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.viewmodel.TravelActivityViewModel
import com.app.myfoottrip.data.viewmodel.TravelViewModel
import com.app.myfoottrip.databinding.FragmentEditSaveTravelBinding
import com.app.myfoottrip.ui.adapter.TravelEditSaveItemAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.EditCustomDialog
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.ui.view.start.JoinBackButtonCustomView
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "EditSaveTravelFragment_싸피"

class EditSaveTravelFragment : BaseFragment<FragmentEditSaveTravelBinding>(
    FragmentEditSaveTravelBinding::bind, R.layout.fragment_edit_save_travel
), OnMapReadyCallback { // End of EditSaveTravelFragment class
    // ViewModel
    private val travelViewModel by viewModels<TravelViewModel>()

    // ActivityViewModel
    private val travelActivityViewModel by activityViewModels<TravelActivityViewModel>()

    lateinit var mapView: MapView
    private lateinit var mContext: Context
    lateinit var visitPlaceRepository: VisitPlaceRepository

    private lateinit var joinBackButtonCustomView: JoinBackButtonCustomView
    private var userVisitPlaceDataList: LinkedList<Place> = LinkedList()
    private var userTravelData: Travel? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var travelEditSaveItemAdapter: TravelEditSaveItemAdapter
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    private lateinit var view2: View
    private var savedInstanceState: Bundle? = null

    // 타입이 0이면 여행 정보 새로 생성, 타입이 2이면 기존의 여행 정보를 불러오기.
    private var fragmentType = 0
    private lateinit var listener: EditCustomDialog.ItemClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context

        try {
            listener = parentFragment as EditCustomDialog.ItemClickListener
        } catch (E: java.lang.ClassCastException) {
            (context.toString() + "must implement NoticeDialogListener")
        }

    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitPlaceRepository = VisitPlaceRepository.get()
    } // End of onCreate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentType = requireArguments().getInt("type")

        view2 = view
        this.savedInstanceState = savedInstanceState

        if (fragmentType == 2) {
            Log.d(TAG, "onViewCreated: 수정 작업니다.")
            updateTravelResponseObserve()
            userTraveLDataDeleteObserve()
        }

        mapView = binding.mapFragment
        mapView.getMapAsync(this)
        locationSource = FusedLocationSource(this, 1000)

        // 첫번째 UI에 데이터 뿌려야 하므로 데이터 부터 가져오기
        CoroutineScope(Dispatchers.IO).launch {
            getData()
        }

        createTravelResponseObserve()
    } // End of onViewCreated

    // 첫번째 UI에 데이터 뿌려야 하므로 데이터 부터 가져오기
    private suspend fun getData() {
        // RoomDB에 데이터를 가져오고나서, Travel타입으로 변환한 후 UI로 뿌리는 작업을 진행한다.
        val dataJob = CoroutineScope(Dispatchers.IO).launch {
            val defferedGetData: Deferred<Int> = async {
                userVisitPlaceDataList = getSqlLiteAllData() as LinkedList<Place>
                1
            }

            defferedGetData.await()
            changeToTravelDto()

            Log.d(TAG, "getData: $userVisitPlaceDataList")

            withContext(Dispatchers.Main) {
                setUI()
                buttonEvents()
                binding.progressBar.visibility = View.GONE
                binding.allConstrainlayout.visibility = View.VISIBLE
                binding.progressBarText.visibility = View.GONE
            }
        }

        dataJob.join()
    } // End of getData

    // 가져온 데이터로 지도에 좌표 마크 표시하기
    private fun setMapInMark() = CoroutineScope(Dispatchers.Main).launch {
        val markers = mutableListOf<Marker>()
        userVisitPlaceDataList.forEach {
            markers += Marker().apply {
                position = LatLng(it.latitude!!, it.longitude!!)
                icon = MarkerIcons.BLACK
                captionText = it.placeName.toString()
            }
        }

        markers.forEach { marker ->
            marker.map = naverMap
            //marker.isFlat = true
            marker.isIconPerspectiveEnabled = true
        }

        val size = userVisitPlaceDataList.size
        if (size >= 2) {
            val polyline = PolylineOverlay()
            val tempList: MutableList<LatLng> = ArrayList()
            for (i in 0 until size) {
                tempList.add(
                    LatLng(
                        userVisitPlaceDataList[i].latitude!!,
                        userVisitPlaceDataList[i].longitude!!
                    )
                )
            }

            polyline.setPattern(10, 5)
            polyline.coords = tempList
            polyline.map = naverMap
        }

        Log.d(TAG, "setMapInMark: $userVisitPlaceDataList")
        Log.d(TAG, "setMapInMark: $size")
    }.onJoin // End of setMapInMark

    private fun changeToTravelDto() {
        var travelId: Int? = null
        if (fragmentType == 2) {
            travelId = travelActivityViewModel.userTravelData.value!!.travelId
        }

        userTravelData = Travel(
            travelId,
            travelActivityViewModel.locationList, userVisitPlaceDataList[0].saveDate, // 처음 시작 저장 시간
            Date(System.currentTimeMillis()), // 마지막 저장 시간
            userVisitPlaceDataList
        )
    } // End of changeToTravelDto

    // 유저의 여행 데이터를 불러와서 UI를 뿌리기
    private fun setUI() = CoroutineScope(Dispatchers.Main).launch {
        // 선택된 지역들을 쉼표로 연결해서 텍스트로 만듬
        binding.tvTravelTitle.text = userTravelData?.location?.joinToString(", ")

        val startDateFormat = SimpleDateFormat("YYYY.MM.dd", Locale("ko", "KR"))
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

        adapterEvent()
    } // End of setUI

    private fun adapterEvent() {
        travelEditSaveItemAdapter.setItemClickListener(object :
            TravelEditSaveItemAdapter.ItemClickListener {
            override fun onEditButtonClick(position: Int, placeData: Place) {
                // 리사이클러뷰 포지션에 해당하는 수정 버튼을 눌렀을 때 이벤트


                if (fragmentType == 2 && userVisitPlaceDataList.size == 1) {
                    val editDialog = EditCustomDialog("수정 작업의 경우 데이터가 없을 경우 해당 데이터가 삭제됩니다. 진행하시나요?")
                    editDialog.show(
                        (activity as AppCompatActivity).supportFragmentManager,
                        "editDialog"
                    )

                    editDialog.setItemClickListener(object : EditCustomDialog.ItemClickListener {
                        override fun onFinishClicked(dialog: DialogFragment) {
                            // 데이터 전체를 새로 UI를 호출함
                            CoroutineScope(Dispatchers.Main).launch {
                                userVisitPlaceDataList.removeAt(position)
                                editDialog.dismiss()
                                CoroutineScope(Dispatchers.IO).launch {
                                    travelDataDelete()
                                }
                            }
                        }

                        override fun onCancelClicked(dialog: DialogFragment) {
                            editDialog.dismiss()
                        }
                    })
                    return
                }

                val editDialog = EditCustomDialog("해당 위치를 삭제하시겠습니까?")
                editDialog.show(
                    (activity as AppCompatActivity).supportFragmentManager,
                    "editDialog"
                )

                editDialog.setItemClickListener(object : EditCustomDialog.ItemClickListener {
                    override fun onFinishClicked(dialog: DialogFragment) {
                        // 데이터 전체를 새로 UI를 호출함
                        CoroutineScope(Dispatchers.Main).launch {
                            userVisitPlaceDataList.removeAt(position)
                            recyclerView.adapter!!.notifyDataSetChanged()
                            editDialog.dismiss()
//                            onMapReady(naverMap)
//                            setUI()
                        }
                    }

                    override fun onCancelClicked(dialog: DialogFragment) {
                        editDialog.dismiss()
                    }
                })

            }
        })
    } // End of adapterEvent


    private fun initRecyclerViewAdapter() {
        travelEditSaveItemAdapter = TravelEditSaveItemAdapter(mContext, userTravelData?.placeList!!)
    } // End of initRecyclerViewAdapter

    private fun totalTimeCalc(): String {
        val diff =
            ((userTravelData!!.startDate!!.time - userTravelData!!.endDate!!.time) / 86_400_000) - 32_400_000
        val dateFormat = SimpleDateFormat("HH시간 mm분", Locale("ko", "KR"))
        return dateFormat.format(diff)
    } // End of totalTimeCalc

    private fun buttonEvents() = CoroutineScope(Dispatchers.IO).launch {
        // 저장 버튼 눌렀을 때 이벤트
        binding.travelEditSaveButton.setOnClickListener {
            if (fragmentType == 2) {
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.allConstrainlayout.visibility = View.GONE
                        binding.progressBarText.visibility = View.VISIBLE
                    }
                    updateTravel()
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d(TAG, "저장할 데이터 : ${userTravelData!!}")
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.allConstrainlayout.visibility = View.GONE
                        binding.progressBarText.visibility = View.VISIBLE
                    }
                    createTravel()
                }
            }
        }

        joinBackButtonCustomView = binding.joinBackButtonCustomview
        joinBackButtonCustomView.findViewById<AppCompatButton>(R.id.custom_back_button_appcompatbutton)
            .setOnClickListener {
                findNavController().popBackStack()
            }
    } // End of buttonEvents


    // ============================================== 유저 데이터 생성 ==============================================
    private suspend fun createTravel() {
        withContext(Dispatchers.Main) {
            if (userVisitPlaceDataList.isEmpty()) {
                requireView().showSnackBarMessage("저장된 좌표가 없으므로 여행 데이터가 저장되지 않았습니다")
                moveResultFragment()
            }
        }

        if (userVisitPlaceDataList.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                // 변환된 Travel데이터를 서버에 저장
                userTravelData?.let { travelViewModel.createTravel(it) }
            }
        }
    } // End of createTravel

    // ============================================== 유저 데이터 수정 ==============================================
    private suspend fun updateTravel() {
        // 수정 작업에서는 마지막 하나를 선택했을 때는 알림창을 띄움
        // (마지막 하나가 삭제되면 Travel자체가 삭제된다는 메시지)
        // 그리고 확인을 누르면 삭제 요청을 보내고 Travel자체가 삭제됨.

        if (userVisitPlaceDataList.size > 1) {
            // 변환된 Travel데이터를 서버에 저장 (수정)
            CoroutineScope(Dispatchers.IO).launch {
                userTravelData?.let {
                    travelViewModel.userTravelDataUpdate(
                        travelActivityViewModel.userTravelData.value!!.travelId!!,
                        it
                    )
                }
            }
        }
    } // End of updateTravel

    private suspend fun travelDataDelete() {
        CoroutineScope(Dispatchers.IO).launch {
            travelViewModel.userTravelDataDelete(travelActivityViewModel.userTravelData.value!!.travelId!!)
        }
    } // End of travelDataDelete

    private suspend fun getSqlLiteAllData(): LinkedList<Place> {
        val placeList: LinkedList<Place> = LinkedList()

        val job = CoroutineScope(Dispatchers.IO).async {
            val allVisitPlaceList = visitPlaceRepository.getAllVisitPlace()

            // SQLLite데이터 전체 가져와서,
            // visitPlace를 변경해서 Place로 변경
            val size = allVisitPlaceList.size
            for (i in 0 until size) {
                val temp = allVisitPlaceList[i]

                placeList.add(
                    Place(
                        null, "", temp.date?.let { Date(it) }, "", // 일단 처음에는 메모 빈 값
                        ArrayList(), // 일단 빈 이미지를 넣어야됨
                        temp.lat, // 좌표
                        temp.lng, // 좌표
                        temp.address
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
                        CoroutineScope(Dispatchers.IO).launch {
                            // 성공하면, SQLLite table 모두 비우기
                            visitPlaceRepository.deleteAllVisitPlace()

                            // DB를 비우고 빠져나가기
                            coroutineScope {
                                withContext(Dispatchers.Main) {
                                    // 다시 여행 선택페이지로 이동
                                    moveResultFragment()
                                }
                            }

                            withContext(Dispatchers.Main) {
                                showToast("저장이 완료되었습니다.")
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

    private fun updateTravelResponseObserve() {
        travelViewModel.userTravelDataUpdateResponseLiveData.observe(viewLifecycleOwner) {
            binding.allConstrainlayout.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE

            when (it) {
                is NetworkResult.Success -> {
                    if (it.data != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            // 성공하면, SQLLite table 모두 비우기
                            visitPlaceRepository.deleteAllVisitPlace()

                            // DB를 비우고 빠져나가기
                            coroutineScope {
                                withContext(Dispatchers.Main) {
                                    moveResultFragment()
                                }
                            }

                            withContext(Dispatchers.Main) {
                                showToast("저장이 완료되었습니다.")
                            }
                        }
                    }
                }

                is NetworkResult.Error -> {
                    binding.root.showSnackBarMessage("유저 여행 데이터 저장 오류 발생")
                    // 다시 여행 선택페이지로 이동
                    val bundle = bundleOf("type" to 0)
                    findNavController().navigate(
                        R.id.action_editSaveTravelFragment_pop,
                        bundle
                    )
                    Log.d(TAG, "userTravelDataUpdateResponseLiveData Error: ${it.data}")
                    Log.d(TAG, "userTravelDataUpdateResponseLiveData Error: ${it.message}")
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "userTravelDataUpdateResponseLiveData Loading")
                    binding.allConstrainlayout.visibility = View.GONE
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    } // End of updateTravelResponseObserve

    private fun userTraveLDataDeleteObserve() {
        travelViewModel.userTravelDataDeleteResponseLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    if (it.data == 204) {
                        moveResultFragment()
                    }
                }

                is NetworkResult.Error -> {
                    requireView().showSnackBarMessage("유저 여행 데이터 삭제 오류 발생")
                    Log.d(TAG, "createTravelResponseLiveData Error: ${it.data}")
                    Log.d(TAG, "createTravelResponseLiveData Error: ${it.message}")
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "createTravelResponseLiveData Loading")
                }
            }
        }
    } // End of userTraveLDataDeleteObserve

    //생성하는 파트이면 좌표가 없을 때 저장이 되지 않음
    private fun moveResultFragment() {
        // 다시 여행 선택페이지로 이동
        val bundle = bundleOf("type" to 0)
        findNavController().navigate(
            R.id.action_editSaveTravelFragment_pop,
            bundle
        )
    } // End of moveResultFragment

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

        val cameraPosition = CameraPosition(
            LatLng(0.0, 0.0),
            16.0, // 줌 레벨
            40.0,
            0.0
        )
        naverMap.cameraPosition = cameraPosition

        setMapInMark()
    } // End of onMapReady
} // End of EditSaveTravelFragment
