package com.app.myfoottrip.ui.view.board

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.databinding.FragmentBoardBinding
import com.app.myfoottrip.ui.adapter.PlaceAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.TimeUtils
import com.naver.maps.map.MapFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import java.sql.Time
import java.util.Date


private const val TAG = "BoardFragment_마이풋트립"
class BoardFragment : BaseFragment<FragmentBoardBinding>(
    FragmentBoardBinding::bind, R.layout.fragment_board){

    private lateinit var mainActivity: MainActivity

    private var mapFragment: MapFragment = MapFragment()

    private lateinit var placeAdapter: PlaceAdapter
    private lateinit var placeList: ArrayList<Place>

    private val boardViewModel by activityViewModels<BoardViewModel>()

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
        initViewPager()
        initPlaceAdapter()
        initMapScroll()
    }

    // Board 객체에 담겨있는 데이터값 화면에 갱신
    private fun initData(){
        binding.apply {
            Log.d(TAG, "initData: ${boardViewModel.board}")

//            tvTravelDate.text = TimeUtils.getDateString(boardViewModel.board.travel.startDate) + TimeUtils.getDateString(boardViewModel.board.travel.endDate)
            tvWriteDate.text = TimeUtils.getDateString(boardViewModel.board.writeDate)
        }
    }

    //게시물 사진 슬라이더
    private fun initViewPager() {

        binding.apply {

            carouselImage.registerLifecycle(viewLifecycleOwner)

            val list = mutableListOf<CarouselItem>().let {
                it.apply {
                    add(CarouselItem(imageUrl = "https://www.innp.co.kr/images/main/07.jpg"))
                    add(CarouselItem(imageUrl = "https://a.cdn-hotels.com/gdcs/production85/d946/73f139d8-4c1d-4ef6-97b0-9b2ccf29878a.jpg?impolicy=fcrop&w=800&h=533&q=medium"))
                    add(CarouselItem(imageUrl = "https://a.cdn-hotels.com/gdcs/production127/d1781/ac9d03ef-22b4-4330-8e8d-695093138cf4.jpg"))
                }
            }

            carouselImage.setData(list)
        }
    }

    //방문 장소 리사이클러뷰 생성
    private fun initPlaceAdapter(){
        placeList = ArrayList()

        val board = arrayOf(
            Place(1,"장생포 고래 문화 특구",Date(System.currentTimeMillis()),"메모", arrayListOf(),20.0,20.0,"대구광역시 수성구"),
            Place(2,"장생포 모노레일",Date(System.currentTimeMillis()),"메모", arrayListOf(),20.0,20.0,"대구광역시 수성구"),
            Place(3,"울산 대교 전망대",Date(System.currentTimeMillis()),"메모", arrayListOf(),20.0,20.0,"대구광역시 수성구"),
            Place(4,"울산 시외버스 터미널",Date(System.currentTimeMillis()),"메모", arrayListOf(),20.0,20.0,"대구광역시 수성구"),
            Place(5,"장생포 모노레일",Date(System.currentTimeMillis()),"메모", arrayListOf(),20.0,20.0,"대구광역시 수성구")
        )

        placeList.addAll(board)

        placeAdapter = PlaceAdapter(placeList)

        placeAdapter.setItemClickListener(object : PlaceAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, boardId: Int) {

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
        mapFragment.getMapAsync{ }

        binding.mapFragment.setTouchListener(object : TouchFrameLayout.OnTouchListener {
            override fun onTouch() {
                // ScrollView Disallow Touch Event
                binding.svBoard.requestDisallowInterceptTouchEvent(true)
            }
        })
    }
}