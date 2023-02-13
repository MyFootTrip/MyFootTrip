package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dao.VisitPlaceRepository
import com.app.myfoottrip.data.dto.*
import com.app.myfoottrip.data.viewmodel.EditSaveViewModel
import com.app.myfoottrip.data.viewmodel.TravelActivityViewModel
import com.app.myfoottrip.data.viewmodel.TravelViewModel
import com.app.myfoottrip.databinding.FragmentEditSaveTravelBinding
import com.app.myfoottrip.ui.adapter.TravelEditSaveItemAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.EditCustomDialog
import com.app.myfoottrip.ui.view.start.JoinBackButtonCustomView
import com.app.myfoottrip.util.ChangeMultipartUtil
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "EditSaveTravelFragment_싸피"

class EditSaveTravelFragment : BaseFragment<FragmentEditSaveTravelBinding>(
    FragmentEditSaveTravelBinding::inflate
), OnMapReadyCallback { // End of EditSaveTravelFragment class
    // ViewModel
    private val travelViewModel by viewModels<TravelViewModel>()
    private val editSaveViewModel by activityViewModels<EditSaveViewModel>()

    // ActivityViewModel
    private val travelActivityViewModel by activityViewModels<TravelActivityViewModel>()

    // Naver지도
    lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    // 마커 배열
    private var markers: MutableList<Marker> = LinkedList()

    // polyline
    private val polyline = PolylineOverlay()


    // Context
    private lateinit var mContext: Context

    // RoomDB
    lateinit var visitPlaceRepository: VisitPlaceRepository

    // BackButton
    private lateinit var joinBackButtonCustomView: JoinBackButtonCustomView

    // 유저 VisitPlaceList (유저가 방문한 좌표 데이터 리스트들 <- 리사이클러뷰에 들어갈 데이터임)
    private var userVisitPlaceDataList: MutableList<VisitPlace> = LinkedList()

    // 전체 Travel데이터 (수정 작업일 경우 가져오는 데이터 이기도 하고, 마지막에 저장할때 쓰이는 데이터임)
    private var userTravelData: Travel? = null
    private var userTravelPushData: TravelPush? = null
    private var userImageList: MutableList<MultipartBody.Part>? = null


    // 리사이클러뷰
    private lateinit var recyclerView: RecyclerView
    private lateinit var travelEditSaveItemAdapter: TravelEditSaveItemAdapter

    // 삭제된 정보들
    var deletePlaceList: MutableList<String> = LinkedList() // 삭제된 이미지 리스트 정보를 담고 있음 구분하지 말고 다본내라.
    var deleteImageList: MutableList<String> = LinkedList() // 삭제된 Place의 아이디를 담는 리스트


    // 타입이 0이면 여행 정보 새로 생성, 타입이 2이면 기존의 여행 정보를 불러오기.
    private var fragmentType = 0
    private lateinit var listener: EditCustomDialog.ItemClickListener

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

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    } // End of onDetach

    // 첫번째 UI에 데이터 뿌려야 하므로 데이터 부터 가져오기
    private suspend fun getData() {
        // RoomDB에 데이터를 가져오고나서, Travel타입으로 변환한 후 UI로 뿌리는 작업을 진행한다.
        val dataJob = CoroutineScope(Dispatchers.IO).launch {
            val defferedGetData: Deferred<Int> = async {
                userVisitPlaceDataList = getSqlLiteAllData()
                1
            }

            defferedGetData.await()
            changeToTravelDto()

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


    private fun clearMapInMark() = CoroutineScope(Dispatchers.Main).launch {
        userVisitPlaceDataList.forEach { mark ->
            markers += Marker().apply {
                position = LatLng(mark.lat, mark.lng)
            }
        }

        markers.forEach { marker ->
            marker.map = null
        }
        polyline.map = null
    }.onJoin

    private fun setMapInMark() = CoroutineScope(Dispatchers.Main).launch {
        markers = mutableListOf()
        userVisitPlaceDataList.forEach {
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

        val size = userVisitPlaceDataList.size
        if (size >= 2) {
            // polyline = PolylineOverlay()
            val tempList: MutableList<LatLng> = ArrayList()
            for (i in 0 until size) {
                tempList.add(
                    LatLng(
                        userVisitPlaceDataList[i].lat, userVisitPlaceDataList[i].lng
                    )
                )
            }

            polyline.setPattern(10, 5)
            polyline.coords = tempList
            polyline.map = naverMap
        }
    }.onJoin // End of setMapInMark


    // 값을 저장하기 위해서 TravelData 형식으로 변환
    private fun changeToTravelDto() {
        var travelId: Int? = null
        if (fragmentType == 2) {
            travelId = travelActivityViewModel.userTravelData.value!!.travelId
        }

        val tempList: MutableList<Place> = ArrayList()
        val size = userVisitPlaceDataList.size
        for (i in 0 until size) {
            val temp = userVisitPlaceDataList[i]

            tempList.add(
                Place(
                    null,
                    temp.placeName,
                    temp.date?.let { Date(it) },
                    temp.content, // 일단 처음에는 메모 빈 값
                    ArrayList(), // 일단 빈 이미지를 넣어야됨
                    temp.lat, // 좌표
                    temp.lng, // 좌표
                    temp.address
                )
            )
        }

        // VisitPlace -> Travel 객체
        userTravelData = Travel(
            travelId,
            travelActivityViewModel.locationList,
            userVisitPlaceDataList[0].date?.let { Date(it) }, // 처음 시작 저장 시간
            Date(System.currentTimeMillis()), // 마지막 저장 시간
            tempList
        )
    } // End of changeToTravelDto

    private fun changePushDto() {
        // 보낼때는 TravelPush와 PlacePush를 사용해라
        var travelId: Int? = null

        if (fragmentType == 2) {
            travelId = travelActivityViewModel.userTravelData.value!!.travelId
        }

        // PlacePush를 담을 List
        val tempList: MutableList<PlacePush> = ArrayList()
        userImageList = LinkedList()

        val size = userVisitPlaceDataList.size
        for (i in 0 until size) {
            val temp = userVisitPlaceDataList[i]

            val tempSize = temp.imgList.size
            for (j in 0 until tempSize) {
                val uri = temp.imgList[j]

                // 이미지 이름이 앞의 4글자가 http로 시작할 경우, 기존에 있던 이미지이므로, 저장하지 않음.
                if (uri.substring(0 until 4) == "http") {
                    continue
                }

                val file = File(
                    ChangeMultipartUtil().changeAbsolutelyPath(
                        Uri.parse(uri), mContext
                    )
                )

                val fileName = "${i}_${j}placePhoto.jpg"
                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                val body = MultipartBody.Part.createFormData("placeImgList", fileName, requestFile)
                userImageList!!.add(body)
            } // End of for(j)

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val simpleDate: String = sdf.format(temp.date!!)
            val simpleDateFormat: Date = sdf.parse(simpleDate) as Date

            var placeId: Int? = null

            // 수정 작업일 때 PlaceID가 없으면 담지 않음
            if (fragmentType == 2) {
                placeId = temp.placeId
            }

            tempList.add(
                PlacePush(
                    placeId, temp.placeName, simpleDateFormat, temp.content, // 일단 처음에는 메모 빈 값
                    ArrayList(), // 일단 빈 이미지를 넣어야됨
                    temp.lat, // 좌표
                    temp.lng, // 좌표
                    temp.address
                )
            )
        } // End of for(i)

        deleteImageList.addAll(editSaveViewModel.deleteImageList)

        // VisitPlace -> Travel 객체
        userTravelPushData = TravelPush(
            travelId,
            travelActivityViewModel.locationList,
            userVisitPlaceDataList[0].date?.let { Date(it) }, // 처음 시작 저장 시간
            Date(System.currentTimeMillis()), // 마지막 저장 시간
            tempList,
            deletePlaceList,
            deleteImageList
        )
    }  // End of changePushDto


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
    } // End of

    private fun adapterEvent() {
        travelEditSaveItemAdapter.setItemClickListener(object :
            TravelEditSaveItemAdapter.ItemClickListener {
            override fun onEditButtonClick(position: Int, placeData: VisitPlace) {
                // 리사이클러뷰 포지션에 해당하는 수정 버튼을 눌렀을 때 이벤트
                // 수정 버튼을 누르면 해당 데이터를 LiveData에 저장하고,
                // 해당 LiveData가 observe되면, 다이얼로그가 켜지는 방식

                editSaveViewModel.setUserVisitPlaceData(userVisitPlaceDataList[position])

                // 다이얼로그의 이벤트를 기준으로 데이터를 처리함.
                val editDialog = EditCustomDialog(userVisitPlaceDataList[position])
                editDialog.show(
                    (activity as AppCompatActivity).supportFragmentManager, "editDialog"
                )

                editDialog.setItemClickListener(object : EditCustomDialog.ItemClickListener {
                    override suspend fun onSaveClicked() {
                        // Nothing
                        editDialog.dismiss()
                    } // End of onSaveClicked

                    override fun onDeleteClicked() {
                        CoroutineScope(Dispatchers.IO).launch {
                            // 삭제하면서 해당 position의 placeId를 먼저가져와서 deletePlaceList에 해당 placeId를 넣는데
                            // placeId가 null값이면 넣지 않음
                            val plId = userVisitPlaceDataList[position].placeId
                            if (plId != null) {
                                deletePlaceList.add(plId.toString())
                            }

                            withContext(Dispatchers.Main) {
                                clearMapInMark()
                            }

                            // RoomDB애서 먼저지움,
                            visitPlaceRepository.deleteVisitPlace(userVisitPlaceDataList[position])
                            userVisitPlaceDataList.removeAt(position)

                            val deffer: Deferred<Int> = async {
                                requireActivity().runOnUiThread {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        setMapInMark()
                                    }
                                }
                                1
                            }

                            val flg = deffer.start()

                            // 좌표 리스트가 비어있지 않을 때는 가장 마지막에 있는 좌표로 지도를 옮김
                            val size = userVisitPlaceDataList.size
                            if (size > 0) {
                                withContext(Dispatchers.Main) {
                                    val cameraPosition = CameraPosition(
                                        LatLng(
                                            userVisitPlaceDataList[size - 1].lat,
                                            userVisitPlaceDataList[size - 1].lng
                                        ), 16.0, // 줌 레벨
                                        40.0, 0.0
                                    )
                                    naverMap.cameraPosition = cameraPosition
                                }
                            }

                            if (!flg) {
                                withContext(Dispatchers.Main) {
                                    recyclerView.adapter!!.notifyDataSetChanged()
                                    editDialog.dismiss()
                                }
                            }
                        }
                    } // End of onDeleteClicked
                }) // End of setItemClickListener
            }
        })
    } // End of adapterEvent

    private fun initRecyclerViewAdapter() {
        travelEditSaveItemAdapter = TravelEditSaveItemAdapter(mContext, userVisitPlaceDataList)
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

            // 수정 작업일 때,
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
//                        val foldingCube: Sprite = FoldingCube()
//                        binding.progressBar.indeterminateDrawable = foldingCube

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

                // 마지막에 저장할 때는 다시 전체를 Travel로 바꿔서 보내야됨
                val defferedGetData: Deferred<Int> = async {
                    userVisitPlaceDataList = getSqlLiteAllData()
                    1
                }

                defferedGetData.await()

                // 통신을 위한 DTO로 수정함
                changePushDto()

                userImageList?.let {
                    userTravelPushData?.let { it1 ->
                        travelViewModel.createTravel(
                            it, it1
                        )
                    }
                }

                // userTravelPushData?.let { travelViewModel.createTravel(it) }
            }
        }
    } // End of createTravel

    // ============================================== 유저 데이터 수정 ==============================================
    private suspend fun updateTravel() {
        // 수정 작업에서는 마지막 하나를 선택했을 때는 알림창을 띄움
        // (마지막 하나가 삭제되면 Travel자체가 삭제된다는 메시지)
        // 그리고 확인을 누르면 삭제 요청을 보내고 Travel자체가 삭제됨.
        Log.d(TAG, "updateTravel: 이거 실행됨?")

        if (userVisitPlaceDataList.size >= 1) {
            // 변환된 Travel데이터를 서버에 저장 (수정)
            CoroutineScope(Dispatchers.IO).launch {

                // 마지막에 저장할 때는 다시 전체를 Travel로 바꿔서 보내야됨
                val defferedGetData: Deferred<Int> = async {
                    userVisitPlaceDataList = getSqlLiteAllData()
                    1
                }

                defferedGetData.await()

                withContext(Dispatchers.IO) {
                    changePushDto()

                    userTravelData?.let {
                        travelViewModel.userTravelDataUpdate(
                            travelActivityViewModel.userTravelData.value!!.travelId!!,
                            userImageList!!,
                            userTravelPushData!!
                        )
                    }
                }
            }
        }
    } // End of updateTravel

    private suspend fun getSqlLiteAllData(): MutableList<VisitPlace> {
        var placeList: MutableList<VisitPlace> = LinkedList()

        val job = CoroutineScope(Dispatchers.IO).async {
            placeList = visitPlaceRepository.getAllVisitPlace()
        }

        job.await()
        job.join()

        return placeList
    } // End of getSqlLiteAllData

    /// TravelData 생성
    private fun createTravelResponseObserve() {
        travelViewModel.createTravelResponseLiveData.observe(this.viewLifecycleOwner) {

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

                            // 유저 생성 ResponseLiveData 다시 초기화
                            travelViewModel.setCreateTravelResponseLiveData(NetworkResult.Success(0))
                        }
                    }
                }

                is NetworkResult.Error -> {
                    binding.root.showSnackBarMessage("유저 여행 데이터 저장 중 오류가 발생하여 작업을 완료하지 못했습니다.")
                    Log.d(TAG, "createTravelResponseLiveData Error: ${it.data}")
                    Log.d(TAG, "createTravelResponseLiveData Error: ${it.message}")
                    moveResultFragment()
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "createTravelResponseLiveData Loading")
                }
            }
        }
    } // End of createTravelResponseObserve

    private fun updateTravelResponseObserve() {
        travelViewModel.userTravelDataUpdateResponseLiveData.observe(this.viewLifecycleOwner) {
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
                        R.id.action_editSaveTravelFragment_pop, bundle
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
        travelViewModel.userTravelDataDeleteResponseLiveData.observe(this.viewLifecycleOwner) {
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
            R.id.action_editSaveTravelFragment_pop, bundle
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
            LatLng(0.0, 0.0), 16.0, // 줌 레벨
            40.0, 0.0
        )
        naverMap.cameraPosition = cameraPosition

        setMapInMark()
    } // End of onMapReady
} // End of EditSaveTravelFragment
