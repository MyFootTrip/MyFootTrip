package com.app.myfoottrip.ui.view.board

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentBoardBinding
import com.app.myfoottrip.ui.adapter.PlaceAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.AlertDialog
import com.app.myfoottrip.ui.view.dialogs.PlaceBottomDialog
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.TimeUtils
import com.app.myfoottrip.util.showSnackBarMessage
import com.bumptech.glide.Glide
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import kotlin.collections.ArrayList


private const val TAG = "BoardFragment_마이풋트립"
class BoardFragment : BaseFragment<FragmentBoardBinding>(
    FragmentBoardBinding::bind, R.layout.fragment_board), OnMapReadyCallback {

    private lateinit var mainActivity: MainActivity

    private var mapFragment: MapFragment = MapFragment()

    private lateinit var placeAdapter: PlaceAdapter

    private val boardViewModel by activityViewModels<BoardViewModel>()
    private val userViewModel by activityViewModels<UserViewModel>()
    private val navigationViewModel by activityViewModels<NavigationViewModel>()
    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navigationViewModel.type == 3) navigationViewModel.type = 1
                else navigationViewModel.type = 0
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        binding.apply {
            ivBack.setOnClickListener { //뒤로가기
                if (navigationViewModel.type == 3) navigationViewModel.type = 1
                else navigationViewModel.type = 0
                findNavController().popBackStack()
            }
            ivComment.setOnClickListener { findNavController().navigate(R.id.action_boardFragment_to_commentFragment)} //댓글 페이지로 이동
            lottieLike.setOnClickListener {
                getLikeObserver()
                getLike()
            }

            //게시글 edit 버튼 클릭 시
            ivEdit.setOnClickListener {
                var popupMenu = PopupMenu(requireContext(),binding.ivEdit)

                mainActivity.menuInflater.inflate(R.menu.comment_menu,popupMenu.menu)

                popupMenu.show()
                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.menu_update -> {
                            findNavController().navigate(R.id.action_boardFragment_to_updateBoardFragment)
                            return@setOnMenuItemClickListener true
                        }
                        R.id.menu_delete -> {
                            showDialog()
                            return@setOnMenuItemClickListener true
                        }else ->{
                        return@setOnMenuItemClickListener true
                    }
                    }
                }
            }
        }

    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun init(){
        getBoardObserver()
        getBoard()
    }

    // Board 객체에 담겨있는 데이터값 화면에 갱신
    private fun initData(board: Board){
        binding.apply {
            // --------------- 게시글 윗쪽 부분 데이터---------------------------
            if(board.userId == userViewModel.wholeMyData.value?.uid) ivEdit.visibility = View.VISIBLE
            initViewPager(board) //이미지 슬라이더
            tvLocation.text = convertToString(board.travel!!.location!! as ArrayList<String>) //여행 지역
            tvTheme.text = "#${board.theme}" //여행 테마
            //여행기간
            tvTravelDate.text = TimeUtils.getDateString(board.travel.startDate!!) +" ~ "+ TimeUtils.getDateString(board.travel.endDate!!)
            tvTitle.text = board.title //제목
            //프로필 이미지
            if (board.profileImg.isNullOrEmpty()){
                ivProfile.setPadding(10)
                Glide.with(this@BoardFragment).load(R.drawable.ic_my).fitCenter().into(ivProfile)
                ivProfile.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white))
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.main)))
            }else {
                Glide.with(this@BoardFragment).load(board.profileImg).centerCrop().into(ivProfile)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white)))
            }
            tvNickname.text = board.nickname //닉네임
            tvWriteDate.text = TimeUtils.getDateString(board.writeDate)  //작성일자

            // -------- 글 후기 부터 밑에 쪽 데이터 -------------
            tvContent.text = board.content
            initPlaceAdapter(board)
            tvLikeCount.text = board.likeList.size.toString()
            tvCommentCount.text = board.commentList.size.toString()

            //좋아요한 게시물인지 체크
            if (boardViewModel.board.value?.data?.likeList!!.contains(userViewModel.wholeMyData.value?.uid)){ //좋아요한 게시물일 때
                lottieLike.progress = 100f
            }else{
                lottieLike.progress = 0f
            }
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
    private fun initViewPager(board: Board) {

        binding.apply {

            carouselImage.registerLifecycle(viewLifecycleOwner)
            carouselImage.imagePlaceholder = ContextCompat.getDrawable(requireContext(),R.raw.loading_image)
            //게시물 사진
            if (board.imageList.isNullOrEmpty()){
                val list = mutableListOf<CarouselItem>().let {
                    it.apply {
                            add(CarouselItem(R.drawable.default_image))
                    }
                }
                carouselImage.setData(list)
            }else{
                val list = mutableListOf<CarouselItem>().let {
                    it.apply {
                        for (image in board.imageList){
                            add(CarouselItem(imageUrl = image))
                        }
                    }
                }
                carouselImage.setData(list)
            }

        }
    }

    //방문 장소 리사이클러뷰 생성
    private fun initPlaceAdapter(board: Board){

        placeAdapter = PlaceAdapter(board.travel?.placeList!!)

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

    //네이버 지도
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
        for (i in boardViewModel.board.value?.data?.travel!!.placeList!!.indices){
            val marker = boardViewModel.board.value?.data?.travel!!.placeList!![i]
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
        if(boardViewModel.board.value?.data?.travel!!.placeList!!.size >= 2){ //좌표가 2개이상일 때 경로 생성
            val path = PolylineOverlay()
            path.coords = markersPosition
            path.color = ContextCompat.getColor(requireContext(),R.color.main)
            path.globalZIndex = 50000
            path.joinType = PolylineOverlay.LineJoin.Bevel
            path.width = 10
            path.map = naverMap
        }

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
    private fun initPlaceBottom(place:Place){
        val placeBottom = PlaceBottomDialog(object : PlaceBottomDialog.OnClickListener {
            override fun onClick(dialog: PlaceBottomDialog) {
                dialog.dismiss()
            }
        },place)
        placeBottom.show(parentFragmentManager, placeBottom.mTag)
    }

    //게시물 삭제 다이얼로그 생성
    private fun showDialog() {
        val dialog = AlertDialog(requireActivity() as AppCompatActivity)

        dialog.setOnOKClickedListener {
            binding.apply {
                deleteBoardObserver()
                deleteBoard()
            }
        }

        dialog.setOnCancelClickedListener { }

        dialog.show("게시물 수정", "게시물을 수정하시겠습니까?")
    }

    //게시물 데이터 받아오기
    private fun getBoard() {
        CoroutineScope(Dispatchers.IO).launch {
            boardViewModel.getBoard(boardViewModel.boardId)
        }
    }

    private fun getBoardObserver() {
        boardViewModel.board.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    initData(it.data!!)
                    boardViewModel.boardId = it.data!!.boardId
                    initMapScroll()
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }

    //게시물 삭제
    private fun deleteBoard() {
        CoroutineScope(Dispatchers.IO).launch {
            boardViewModel.deleteBoard(boardViewModel.boardId)
        }
    }

    private fun deleteBoardObserver() {
        boardViewModel.deleteBoard.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    navigationViewModel.type = 4
                    findNavController().navigate(R.id.action_boardFragment_to_mainFragment)
                    binding.root.showSnackBarMessage("게시물이 삭제되었습니다.")
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }

    //좋아요 상태 받아오기
    private fun getLike() {
        CoroutineScope(Dispatchers.IO).launch {
            val message = "${userViewModel.wholeMyData.value?.join?.nickname}님이 ${boardViewModel.board.value?.data?.title}에 좋아요를 눌렀습니다! ❤"
            boardViewModel.likeBoard(boardViewModel.boardId,message)
        }
    }

    private fun getLikeObserver() {
        // viewModel에서 전체 게시글 데이터 LiveData 옵저버 적용
        boardViewModel.likeCheck.observe(viewLifecycleOwner) {
            binding.lottieLike.isEnabled = false //터치 불가능하게 만들기
            when (it) {
                is NetworkResult.Success -> {
                    if (it.data == true) {
                        binding.lottieLike.playAnimation()
                        if (boardViewModel.board.value?.data?.likeList!!.contains(userViewModel.wholeMyData.value?.uid)){ //좋아요한 게시물일 때
                            binding.tvLikeCount.text = "${boardViewModel.board.value?.data?.likeList!!.size}"
                        }else binding.tvLikeCount.text = "${boardViewModel.board.value?.data?.likeList!!.size + 1}"
                    }
                    else if(it.data == false) {
                        binding.lottieLike.progress = 0f
                        binding.lottieLike.pauseAnimation()
                        if (boardViewModel.board.value?.data?.likeList!!.contains(userViewModel.wholeMyData.value?.uid)){ //좋아요한 게시물일 때
                            binding.tvLikeCount.text = "${boardViewModel.board.value?.data?.likeList!!.size-1}"
                        }else binding.tvLikeCount.text = "${boardViewModel.board.value?.data?.likeList!!.size}"
                    }
                    binding.lottieLike.isEnabled = true //터치 가능하게 만들기

                }
                is NetworkResult.Error -> {}
                is NetworkResult.Loading -> {}
            }
        }
    }
}