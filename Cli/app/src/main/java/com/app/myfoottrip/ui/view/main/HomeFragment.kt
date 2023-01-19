package com.app.myfoottrip.ui.view.main

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.databinding.FragmentHomeBinding
import com.app.myfoottrip.ui.adapter.CategoryAdatper
import com.app.myfoottrip.ui.adapter.HomeAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.util.NetworkResult
import com.forms.sti.progresslitieigb.ProgressLoadingIGB
import com.forms.sti.progresslitieigb.finishLoadingIGB
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "HomeFragment_마이풋트립"

class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::bind, R.layout.fragment_home
) {

    private lateinit var mainActivity: MainActivity

    private lateinit var categoryAdapter: CategoryAdatper
    private lateinit var detailList: ArrayList<String>
    private lateinit var selectedDetailList : ArrayList<String>

    private lateinit var homeAdatper: HomeAdapter

    private var sortBy = "최신순" //정렬 기준

    private val boardViewModel by activityViewModels<BoardViewModel>()

    var detector: GestureDetector? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        //게시물 작성 페이지로 이동
        binding.ivWrite.setOnClickListener { findNavController().navigate(R.id.action_mainFragment_to_createBoardFragment) }

    }

    private fun init() {
        getBoardListObserver()
        initSpinnerSort()
        initChips()
        detectScroll()
        touchLayout()
        setUpSwipeRefresh()
        getData()
    }

    private fun startLoading(){
        ProgressLoadingIGB.startLoadingIGB(requireContext()){
            message = "Good Morning!" //  Center Message
            srcLottieJson = R.raw.loading_walk // Tour Source JSON Lottie
            timer = 10000                   // Time of live for progress.
            hight = 500 // Optional
            width = 500 // Optional
        }
    }

    private fun initHomeAdapter(boardList : ArrayList<Board>){

        //정렬 기준
        if(sortBy == "최신순") boardList.sortByDescending { it.writeDate }
        else boardList.sortByDescending { it.likeCount }

        homeAdatper = HomeAdapter(boardList)

        homeAdatper.setItemClickListener(object : HomeAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, boardId: Int) {
                boardViewModel.board = boardList[position]
                findNavController().navigate(R.id.action_mainFragment_to_boardFragment)
            }
        })

        binding.rvHome.apply {
            adapter = homeAdatper
            //원래의 목록위치로 돌아오게함
            homeAdatper.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    //필터 리사이클러뷰 생성
    private fun initFilterAdapter(detailId: Int) {

        detailList = ArrayList()

        when (detailId) { //해당 필터에 대한 기준을 detailList에 추가
            1 -> { //여행 테마
                detailList.addAll(themeList)
            }
            2 -> { //여행 지역
                detailList.addAll(locationList)
            }
            3 -> { //여행 기간
                detailList.addAll(periodList)
            }
            else -> { //연령대
                detailList.addAll(ageList)
            }
        }

        categoryAdapter = CategoryAdatper(detailList)

        selectedDetailList = ArrayList()

        categoryAdapter.setItemClickListener(object : CategoryAdatper.ItemClickListener {
            override fun onClick(view: View, position: Int) {

                if (!selectedDetailList.contains(detailList[position])) {
                    selectedDetailList.add(detailList[position])

                    binding.cgDetail.addView(Chip(requireContext()).apply {
                        chipCornerRadius = 10.0f
                        text = detailList[position]
                        textSize = 12.0f
                        isCloseIconVisible = true
                        closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
                        closeIconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.main))
                        setTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)))
                        setOnCloseIconClickListener {
                            binding.cgDetail.removeView(this)
                            selectedDetailList.remove(detailList[position])
                        }
                    })
                }
            }
        })

        binding.rvCategory.apply {
            adapter = categoryAdapter
        }
    }


    //필터 생성 메소드
    private fun initChips() {
        binding.apply {
            cgCategory.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.chip_theme -> initDetail(checkedId)
                    R.id.chip_location -> initDetail(checkedId)
                    R.id.chip_period -> initDetail(checkedId)
                    R.id.chip_age -> initDetail(checkedId)
                    else -> binding.rvCategory.visibility = View.GONE
                }
            }
        }
    }

    //필터 클릭 시 상세 필터 기준 생성
    private fun initDetail(checkedId: Int) {
        binding.apply {
            when (checkedId) {
                R.id.chip_theme -> {
                    initFilterAdapter(1)
                    binding.rvCategory.visibility = View.VISIBLE
                }
                R.id.chip_location -> {
                    initFilterAdapter(2)
                    binding.rvCategory.visibility = View.VISIBLE
                }
                R.id.chip_period -> {
                    initFilterAdapter(3)
                    binding.rvCategory.visibility = View.VISIBLE
                }
                else -> {
                    initFilterAdapter(4)
                    binding.rvCategory.visibility = View.VISIBLE
                }
            }

        }
    }

    //최신순 좋아요순 스피너 생성
    private fun initSpinnerSort(){
        binding.apply {
            spinnerSort.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
                sortBy = newItem //현재 정렬기준 갱신
                getData()
                getBoardListObserver()
            }
        }
    }

    //스크롤 감지
    private fun detectScroll() {
        detector = GestureDetector(requireContext(), object : GestureDetector.OnGestureListener {
            //화면이 눌렸을 때
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            //화면이 눌렸다 떼어지는 경우
            override fun onShowPress(e: MotionEvent) {
            }

            //화면이 한 손가락으로 눌렸다 떼어지는 경우
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            //화면이 눌린채 일정한 속도와 방향으로 움직였다 떼어지는 경우
            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                binding.cgCategory.clearCheck()
                return false
            }

            //화면을 손가락으로 오랫동안 눌렀을 경우
            override fun onLongPress(e: MotionEvent) {
            }

            //화면이 눌린채 손가락이 가속해서 움직였다 떼어지는 경우
            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                return true
            }
        })
    }

    //화면 터치 이벤트
    private fun touchLayout(){
        binding.root.setOnTouchListener { _, event ->
            detector!!.onTouchEvent(event)
        }

        val onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                binding.cgCategory.clearCheck()
            }
        }

        //리사이클러 뷰 영역에 스크롤 감지 리스너 등록
        binding.rvHome.addOnScrollListener(onScrollListener)
    }

    //새로고침 이벤트
    private fun setUpSwipeRefresh() {
        //새로고침 리사이클러뷰의 어댑터를 통해 불러온 List와 원래 refreshItemList이 다른 주소를 가지고 있었음.
        binding.swipeLayout.setOnRefreshListener {
            //필터 부분
            binding.cgCategory.clearCheck()
            binding.cgDetail.removeAllViews()


            //정렬 부분
            binding.spinnerSort.selectItemByIndex(0)
            sortBy = "최신순"

            //데이터 요청
            getData()
            getBoardListObserver()
            binding.swipeLayout.isRefreshing = false
        }
    }

    // ----------------Retrofit------------------
    //게시물 전체 받아오기
    private fun getData(){
        CoroutineScope(Dispatchers.IO).launch {
            boardViewModel.getBoardList()
        }
    }

    private fun getBoardListObserver() {
        // viewModel에서 전체 게시글 데이터 LiveData 옵저버 적용
        boardViewModel.boardList.observe(viewLifecycleOwner) {
            mainActivity.finishLoadingIGB()
            when (it) {
                is NetworkResult.Success -> {
                    initHomeAdapter(it.data as ArrayList<Board>)
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "게시물 조회 Error: ${it.data}")
                }
                is NetworkResult.Loading -> {
                    startLoading()
                }
            }
        }
    } // 게시물 전체 받아오기

    companion object {
        val themeList = arrayOf("혼자 왔니","커플 여행","효도 하자","우정 여행","직장 동료와 함께","가족과 같이")
        val locationList = arrayOf("서울","경기","강원","부산","경북·대구","전남·광주","제주","충남·대전","경남","충북","경남","전북","인천")
        val periodList = arrayOf("당일 치기","1박 2일","2박 3일","3박 4일","4박 5일 이상")
        val ageList = arrayOf("10대", "20대", "30대", "40대", "50대", "60대 이상")
    }
}