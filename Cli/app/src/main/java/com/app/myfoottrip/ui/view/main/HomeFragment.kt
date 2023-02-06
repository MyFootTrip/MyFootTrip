package com.app.myfoottrip.ui.view.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Filter
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.databinding.FragmentHomeBinding
import com.app.myfoottrip.ui.adapter.CategoryAdatper
import com.app.myfoottrip.ui.adapter.HomeAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.util.CommonUtils
import com.app.myfoottrip.util.NetworkResult
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "HomeFragment_싸피"

class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::bind, R.layout.fragment_home
) {

    private lateinit var mContext: Context
    private lateinit var categoryAdapter: CategoryAdatper
    private lateinit var detailList: ArrayList<String>
    private var selectedDetailList: ArrayList<String> = ArrayList()
    private lateinit var homeAdatper: HomeAdapter
    private var sortBy = "최신순" //정렬 기준
    private val boardViewModel by activityViewModels<BoardViewModel>()
    private var filter: Filter = Filter(arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf())
    var detector: GestureDetector? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** DynamicLink 수신확인 */
        initDynamicLink()

        init()

        //게시물 작성 페이지로 이동
        binding.ivWrite.setOnClickListener {
            val bundle = bundleOf("type" to 2)
            binding.spinnerSort.dismiss()
            findNavController().navigate(R.id.action_mainFragment_to_travelSelectFragment, bundle)
        }

//        viewLifecycleOwner.lifecycleScope.launch {
////            whenStarted {
////          }
//        }
    } // End of onViewCreated

    override fun onResume() {
        super.onResume()
        init()
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

    private fun initHomeAdapter(boardList: ArrayList<Board>) {
        //정렬 기준
        if(sortBy == "최신순") boardList.sortByDescending { it.writeDate }
        else boardList.sortByDescending { it.likeList.size }

        homeAdatper = HomeAdapter(boardList)

        homeAdatper.setItemClickListener(object : HomeAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, boardId: Int) {
                boardViewModel.boardId = boardId
                binding.spinnerSort.dismiss()
                findNavController().navigate(R.id.action_mainFragment_to_boardFragment)
            }
        })

        binding.rvHome.apply {
            adapter = homeAdatper
            //원래의 목록위치로 돌아오게함
            homeAdatper.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    } // End of initHomeAdapter

    //필터 리사이클러뷰 생성
    private fun initFilterAdapter(detailId: Int) {

        detailList = ArrayList()

        when (detailId) { //해당 필터에 대한 기준을 detailList에 추가
            1 -> { //여행 테마
                detailList.addAll(THEME_LIST)
            }
            2 -> { //여행 지역
                detailList.addAll(LOCATION_LIST)
            }
            3 -> { //여행 기간
                detailList.addAll(PERIOD_LIST)
            }
            else -> { //연령대
                detailList.addAll(AGE_LIST)
            }
        }

        categoryAdapter = CategoryAdatper(detailList)

        categoryAdapter.setItemClickListener(object : CategoryAdatper.ItemClickListener {
            override fun onClick(view: View, position: Int, category: String) {
                if (!selectedDetailList.contains(category)) {
                    //필터에 어떤 유형으로 필터링할 것인지 삽입
                    when (detailId) {
                        1 -> { //여행 테마
                            filter.themeList.add(category)
                        }
                        2 -> { //여행 지역
                            filter.regionList.add(category)
                        }
                        3 -> { //여행 기간
                            filter.periodList.add(category)
                        }
                        else -> { //연령대
                            filter.ageList.add(category)
                        }
                    }

                    selectedDetailList.add(category)
                    binding.cgDetail.addView(Chip(requireContext()).apply {
                        chipCornerRadius = 10.0f
                        text = category
                        textSize = 12.0f
                        isCloseIconVisible = true
                        closeIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
                        closeIconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.main))
                        setTextColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)))
                        setOnCloseIconClickListener {
                            binding.cgDetail.removeView(this)
                            selectedDetailList.remove(category)

                            //필터에서 해당 필터유형 삭제
                            if (filter.themeList.isNotEmpty() && filter.themeList.contains(category)) filter.themeList.remove(
                                category
                            )
                            else if (filter.regionList.isNotEmpty() && filter.regionList.contains(
                                    category
                                )
                            ) filter.regionList.remove(category)
                            else if (filter.periodList.isNotEmpty() && filter.periodList.contains(
                                    category
                                )
                            ) filter.periodList.remove(category)
                            else if (filter.ageList.isNotEmpty() && filter.ageList.contains(category)) filter.ageList.remove(
                                category
                            )
                            getFilterdData(filter)
                            getFilteredBoardListObserver()
                        }
                    })
                    getFilterdData(filter)
                    getFilteredBoardListObserver()
                }
            }
        })


        binding.rvCategory.apply {
            adapter = categoryAdapter
        }
    } // End of initFilterAdapter

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
    } // End of initChips

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
    } // End of initDetail

    //최신순 좋아요순 스피너 생성
    private fun initSpinnerSort() {
        binding.apply {
            spinnerSort.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
                sortBy = newItem //현재 정렬기준 갱신
                getData()
                getBoardListObserver()
            }
        }
    } // End of initSpinnerSort



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
            override fun onScroll(
                e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float
            ): Boolean {
                binding.cgCategory.clearCheck()
                return false
            }

            //화면을 손가락으로 오랫동안 눌렀을 경우
            override fun onLongPress(e: MotionEvent) {
            }

            //화면이 눌린채 손가락이 가속해서 움직였다 떼어지는 경우
            override fun onFling(
                e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float
            ): Boolean {
                return true
            }
        })
    } // End of detectScroll

    //화면 터치 이벤트
    @SuppressLint("ClickableViewAccessibility")
    private fun touchLayout() {
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
                binding.spinnerSort.dismiss()
            }
        }

        //리사이클러 뷰 영역에 스크롤 감지 리스너 등록
        binding.rvHome.addOnScrollListener(onScrollListener)
    } // End of touchLayout

    //새로고침 이벤트
    private fun setUpSwipeRefresh() {
        //새로고침 리사이클러뷰의 어댑터를 통해 불러온 List와 원래 refreshItemList이 다른 주소를 가지고 있었음.
        binding.swipeLayout.setOnRefreshListener {
            //필터 부분
            binding.cgCategory.clearCheck()
            binding.cgDetail.removeAllViews()
            filter = Filter(arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf())

            //정렬 부분
            binding.spinnerSort.selectItemByIndex(0)
            sortBy = "최신순"
            selectedDetailList.clear()

            //데이터 요청
            getData()
            getBoardListObserver()
            binding.swipeLayout.isRefreshing = false
        }
    }

    //게시물 전체 받아오기
    private fun getData() {
        CoroutineScope(Dispatchers.IO).launch {
            boardViewModel.getBoardList()
        }
    }

    private fun getBoardListObserver() {
        // viewModel에서 전체 게시글 데이터 LiveData 옵저버 적용
        boardViewModel.boardList.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    initHomeAdapter(it.data as ArrayList<Board>)
                    binding.lottieHome.pauseAnimation()
                    binding.lottieHome.visibility = View.INVISIBLE
                    binding.tvPlanCount.text = CommonUtils.makeComma(it.data!!.size)
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "게시물 조회 Error: ${it.data}")
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    } // 게시물 전체 받아오기

    //필러팅된 게시물 받아오기
    private fun getFilterdData(filter: Filter) {
        CoroutineScope(Dispatchers.IO).launch {
            boardViewModel.getFilteredBoardList(filter)
        }
    }

    private fun getFilteredBoardListObserver() {
        boardViewModel.boardList.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    initHomeAdapter(it.data as ArrayList<Board>)
                    binding.lottieHome.pauseAnimation()
                    binding.lottieHome.visibility = View.INVISIBLE
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "게시물 조회 Error: ${it.data}")
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    } // 게시물 전체 받아오기

    private fun initDynamicLink() {
        val dynamicLinkData = requireActivity().intent.extras
        if (dynamicLinkData != null) {
            var dataStr = "DynamicLink 수신받은 값\n"
            for (key in dynamicLinkData.keySet()) {
                dataStr += "key: $key / value: ${dynamicLinkData.getString(key)}\n"
            }

//            binding.tvToken.text = dataStr
            Log.d(TAG, "initDynamicLink: $dataStr")
        }
    }

    fun dismissSpinner(){
        binding.spinnerSort.dismiss()
    }

    companion object {
        val THEME_LIST = arrayOf("혼자", "친구와", "연인과", "배우자와", "아이와", "부모님과", "기타")
        val LOCATION_LIST = arrayOf("서울", "경기", "강원", "부산", "경북·대구", "전남·광주", "제주", "충남·대전", "경남", "충북", "전북", "인천")
        val PERIOD_LIST = arrayOf("당일 치기", "1박 2일", "2박 3일", "3박 4일", "4박 5일+")
        val AGE_LIST = arrayOf("10대", "20대", "30대", "40대", "50대", "60대 이상")
    }
} // End of HomeFragment class
