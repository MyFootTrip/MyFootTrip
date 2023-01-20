package com.app.myfoottrip.ui.view.travel


import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.viewmodel.TravelViewModel
import com.app.myfoottrip.databinding.FragmentTravelSelectBinding
import com.app.myfoottrip.ui.adapter.TravelAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "TravelSelectFragment_싸피"

class TravelSelectFragment : BaseFragment<FragmentTravelSelectBinding>(
    FragmentTravelSelectBinding::bind, R.layout.fragment_travel_select
) {
    private var type = 0 // 0 : 여정 기록, 1 : 마이페이지, 2 : 게시글 작성

    private val travelViewModel by activityViewModels<TravelViewModel>()
    private lateinit var travelAdapter: TravelAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //type 받는 코드
        type = requireArguments().getInt("type")
        Log.d(TAG, "onViewCreated: type : $type")
        initialize()
    } // End of onViewCreated

    private fun initialize() {

        // 데이터를 가져오는 옵저버
        userTravelDataObserver()

        setData()

        if (type == 0) { //여정 선택 부분
            binding.tvTravelTitle.text = "여정을 선택해주세요."
            binding.btnSave.visibility = View.VISIBLE
            binding.btnSave.text = "+ 여정 새로 만들기"
        } else if (type == 1) { //여정 관리 부분
            binding.tvTravelTitle.text = "나의 여정"
            binding.btnSave.visibility = View.GONE
        } else { //게시글
            binding.tvTravelTitle.text = "여정 선택하기"
            binding.btnSave.visibility = View.VISIBLE
            binding.btnSave.text = "여정을 선택해주세요"
            binding.btnSave.isEnabled = false
            binding.btnSave.setTextColor(
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.main
                    )
                )
            )
            binding.btnSave.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.gray_bright
                )
            )
        }
        initAdapter()
        setListener()
    } // End of initialize

    private fun initAdapter() {
        travelAdapter = TravelAdapter(arrayListOf(), type)
        binding.rvTravel.adapter = travelAdapter

        travelAdapter.setItemClickListener(object : TravelAdapter.ItemClickListener {
            override fun onAllClick(position: Int, travelDto: Travel) {
                //TODO : 여정 확인 페이지로 이동
            }

            override fun onChipClick(type: Int, position: Int, travelDto: Travel) {
                if (type == 0 || type == 2) { //여정 선택
                    changeSelected(position)
                } else { //여정 삭제
                    //TODO : 여정 삭제 dialog
                }
            }

        })
    } // End of initAdapter

    private fun setListener() {
        binding.apply {
            ivBack.setOnClickListener {
//                findNavController().popBackStack()
            }
            btnSave.setOnClickListener {
                //TODO : select 된 상태이면 -> 하는거

                when (type) {
                    0 -> findNavController().navigate(R.id.action_travelSelectFragment_to_travelLocationSelectFragment)
                    2 -> findNavController().navigate(R.id.action_travelSelectFragment_to_createBoardFragment)
                }
            }
        }
    } // End of setListener

    private fun userTravelDataObserver() {
        Log.d(TAG, "userTravelDataObserver: 옵저버 등록")
        travelViewModel.travelUserData.observe(viewLifecycleOwner) {
            Log.d(TAG, "userTravelDataObserver: 옵저버 실행")

            when (it) {
                is NetworkResult.Success -> {
                    if (it.data != null) {
                        val boardList = it.data as ArrayList<Travel>
                        travelAdapter.setList(boardList)
                        travelAdapter.notifyDataSetChanged()
                    }
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "userTravel 체크 Error: ${it.data}")
                }
                is NetworkResult.Loading -> {
                    //TODO: 로딩바
                }
            }
        }
    } // End of userTravelDataObserver


    private fun setData() { //TODO : DB에서 값 가져와서 넣기
        travelViewModel.getUserTravel(1)
    } // End of setData

    private fun changeSelected(position: Int) {
        travelAdapter.run {

            var lastPos = getSelected()
            if (position == lastPos) { //선택 해제
                setSelected(-1)
                settingPage(false)
                if (type == 2) binding.btnSave.isEnabled = false
                binding.btnSave.setTextColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.main
                        )
                    )
                )
                binding.btnSave.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.gray_bright
                    )
                )
            } else { //선택
                setSelected(position)
                settingPage(true)
                if (type == 2) binding.btnSave.isEnabled = true
                binding.btnSave.setTextColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                )
                binding.btnSave.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.main))
            }
            notifyItemChanged(lastPos) // 선택 해제
            notifyItemChanged(position) // 선택
        }
    }

    private fun settingPage(isChecked: Boolean) {
        if (isChecked) {
            when (type) {
                0 -> {
                    binding.btnSave.text = "선택 완료"
                }
                2 -> {
                    binding.btnSave.text = "작성 하기"
                }
            }
        } else {
            when (type) {
                0 -> {
                    binding.btnSave.text = "+ 여정 새로 만들기"
                }
                2 -> {
                    binding.btnSave.text = "여정을 선택해주세요"
                }
            }
        }
    }
}