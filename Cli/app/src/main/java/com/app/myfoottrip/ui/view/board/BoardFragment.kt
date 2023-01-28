package com.app.myfoottrip.ui.view.board

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.databinding.FragmentBoardBinding
import com.app.myfoottrip.ui.adapter.PlaceAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.CommentInputDialog
import com.app.myfoottrip.ui.view.dialogs.PlaceBottomDialog
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.TimeUtils
import com.bumptech.glide.Glide
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.ArrowheadPathOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.overlay.PolylineOverlay
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import java.util.*
import kotlin.collections.ArrayList


private const val TAG = "BoardFragment_마이풋트립"
class BoardFragment : BaseFragment<FragmentBoardBinding>(
    FragmentBoardBinding::bind, R.layout.fragment_board), OnMapReadyCallback {

    private lateinit var mainActivity: MainActivity

    private var mapFragment: MapFragment = MapFragment()

    private lateinit var placeAdapter: PlaceAdapter

    private val boardViewModel by activityViewModels<BoardViewModel>()

    //지도 객체 변수
    private lateinit var mapView: MapView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack()} //뒤로가기
            ivComment.setOnClickListener {findNavController().navigate(R.id.action_boardFragment_to_commentFragment)} //댓글 페이지로 이동
        }

    }

    private fun init(){
        initData()
        initMapScroll()
    }

    // Board 객체에 담겨있는 데이터값 화면에 갱신
    private fun initData(){
        binding.apply {
            // --------------- 게시글 윗쪽 부분 데이터---------------------------
            initViewPager() //이미지 슬라이더
            tvLocation.text = convertToString(boardViewModel.board.travel!!.location!! as ArrayList<String>) //여행 지역
            tvTheme.text = "#${boardViewModel.board.theme}" //여행 테마
            //여행기간
            tvTravelDate.text = TimeUtils.getDateString(boardViewModel.board.travel?.startDate!!) +" ~ "+ TimeUtils.getDateString(boardViewModel.board.travel?.endDate!!)
            tvTitle.text = boardViewModel.board.title //제목
            Glide.with(this@BoardFragment).load(boardViewModel.board.profileImg).centerCrop().into(ivProfile)
            tvNickname.text = boardViewModel.board.nickname //닉네임
            tvWriteDate.text = TimeUtils.getDateString(boardViewModel.board.writeDate)  //작성일자

            // -------- 글 후기 부터 밑에 쪽 데이터 -------------
            tvContent.text = boardViewModel.board.content
            initPlaceAdapter()
        }
    }

    //테마 및 지역 #붙인 스트링으로 만들기
    private fun convertToString(stringList : ArrayList<String>) : String{
        val sb = StringBuilder()
        for (str in stringList){
            sb.append("#${str} ")
        }
        return sb.toString()
    }

    //게시물 사진 슬라이더
    private fun initViewPager() {

        binding.apply {

            carouselImage.registerLifecycle(viewLifecycleOwner)

            val list = mutableListOf<CarouselItem>().let {
                it.apply {
                    for (image in boardViewModel.board.imageList){
                        add(CarouselItem(imageUrl = image))
                    }
                }
            }

            carouselImage.setData(list)
        }
    }

    //방문 장소 리사이클러뷰 생성
    private fun initPlaceAdapter(){

        placeAdapter = PlaceAdapter(boardViewModel.board.travel?.placeList!!)

        placeAdapter.setItemClickListener(object : PlaceAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, place: Place) {
                initPlaceBottom(place)
            }
        })

        binding.rvPlace.apply {
            adapter = placeAdapter
            //원래의 목록위치로 돌아오게함
            placeAdapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    //지도 뷰 스크롤 터치 제어
    private fun initMapScroll(){
        //TouchFrameLayout 에 mapFragment 올려놓기
        val fragmentTransaction = childFragmentManager.beginTransaction()
        if(mapFragment.isAdded){
            fragmentTransaction.remove( mapFragment )
            mapFragment = MapFragment()
        }
        fragmentTransaction.add(R.id.map_fragment, mapFragment).commit()
        mapFragment.getMapAsync(this)

        binding.mapFragment.setTouchListener(object : TouchFrameLayout.OnTouchListener {
            override fun onTouch() {
                // ScrollView Disallow Touch Event
                binding.svBoard.requestDisallowInterceptTouchEvent(true)
            }
        })
    }

    //네이버 지도 마커 표시시
   override fun onMapReady(naverMap: NaverMap) {
        val options = NaverMapOptions()
            .camera(CameraPosition(LatLng(36.02539, 128.380378),  10.0))  // 카메라 위치 (위도,경도,줌)
            .mapType(NaverMap.MapType.Basic)    //지도 유형
            .enabledLayerGroups(NaverMap.LAYER_GROUP_BUILDING)  //빌딩 표시

        MapFragment.newInstance(options)

        binding.tvMarkerNum.visibility = View.GONE


        val markers = mutableListOf<Marker>()
        val markersPosition = ArrayList<LatLng>()


        //마커 생성
        for (i in boardViewModel.board.travel!!.placeList!!.indices){
            val marker = boardViewModel.board.travel!!.placeList!![i]
            markers.add(
                Marker().apply {
                    binding.tvMarkerNum.text = "${i+1}"
                    position = LatLng(marker.latitude!!,marker.longitude!!)
                    markersPosition.add(position)
                    icon = OverlayImage.fromView(binding.tvMarkerNum)
                    width = 70
                    height = 70
                    isHideCollidedMarkers = true
                    isForceShowIcon = true
                    isHideCollidedSymbols = true
                    map = naverMap
                }
            )
        }

        //경로 표시
        val path = PolylineOverlay()
        path.coords = markersPosition
        path.color = ContextCompat.getColor(requireContext(),R.color.main)
        path.globalZIndex = 50000
        path.joinType = PolylineOverlay.LineJoin.Bevel
        path.width = 10
        path.map = naverMap


        //카메라 영역 제어
        val bounds = LatLngBounds.Builder()
            .include(markersPosition)
            .build()

        val cameraUpdate = CameraUpdate.fitBounds(bounds,100).animate(CameraAnimation.Easing)
        naverMap.moveCamera(cameraUpdate)

        naverMap.minZoom = 1.0
        naverMap.maxZoom = 18.0

    }

    // 장소 바텀시트 다이얼로그 생성
    //댓글 입력창 생성
    private fun initPlaceBottom(place:Place){
        val placeBottom = PlaceBottomDialog(object : PlaceBottomDialog.OnClickListener {
            override fun onClick(dialog: PlaceBottomDialog) {
                dialog.dismiss()
            }
        },place)

        placeBottom.show(parentFragmentManager, placeBottom.mTag)
    }
}