package com.app.myfoottrip.ui.view.travel

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.databinding.FragmentTravelSelectBinding
import com.app.myfoottrip.ui.adapter.TravelAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import java.text.FieldPosition
import java.util.*
import kotlin.collections.ArrayList

class TravelSelectFragment : BaseFragment<FragmentTravelSelectBinding>(
    FragmentTravelSelectBinding::bind, R.layout.fragment_travel_select
) {
    private var type = 0 // 0 : 여정 선택, 1: 여정 보기

    private var boardList : ArrayList<Travel> = arrayListOf()
    private lateinit var travelAdapter: TravelAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize(){
        if(type == 0){ //여정 선택 부분
            binding.tvTravelTitle.text = "여정을 선택해주세요"
            binding.btnSave.visibility = View.VISIBLE
            binding.btnSave.text = "+ 여정 새로 만들기"
        }else{ //여정 관리 부분
            binding.tvTravelTitle.text = "나의 여정"
            binding.btnSave.visibility = View.GONE
        }
        dumiSet()
        initAdapter()
        setListener()
    }

    private fun initAdapter(){
        travelAdapter = TravelAdapter(boardList, type)
        binding.rvTravel.adapter = travelAdapter

        travelAdapter.setItemClickListener(object : TravelAdapter.ItemClickListener{
            override fun onAllClick(position: Int, travelDto: Travel) {
                //TODO : 여정 확인 페이지로 이동
            }

            override fun onChipClick(type: Int, position: Int, travelDto: Travel) {
                if(type == 0){ //여정 선택
                    changeSelected(position)
                }else{ //여정 삭제
                    //TODO : 여정 삭제 dialog
                }
            }

        })
    }

    private fun setListener(){
        binding.apply {
            ivBack.setOnClickListener{
                findNavController().popBackStack()
            }
            btnSave.setOnClickListener{
                //TODO : select 된 상태이면 -> 하는거
                findNavController().navigate(R.id.action_travelSelectFragment_to_travelLocationSelectFragment)
            }
        }
    }

    private fun dumiSet(){
        boardList.add(
            Travel("0", "서울, 부산", Date(1234564),Date(12645648), arrayListOf()),
        )
        boardList.add(
            Travel("1", "대구, 부산", Date(16548946),Date(23551651),arrayListOf()),
        )
        boardList.add(
            Travel("1", "대구, 부산", Date(16548946),Date(23551651), arrayListOf()),
        )
        boardList.add(
            Travel("1", "대구, 부산", Date(16548946),Date(23551651),arrayListOf()),
        )
        boardList.add(
            Travel("1", "대구, 부산", Date(16548946),Date(23551651), arrayListOf()),
        )
        boardList.add(
            Travel("1", "대구, 부산", Date(16548946),Date(23551651),arrayListOf()),
        )
        boardList.add(
            Travel("1", "대구, 부산", Date(16548946),Date(23551651), arrayListOf()),
        )
    }

    private fun changeSelected(position: Int){
        travelAdapter.apply { 
            var lastPos = getSelected()
            if(position == lastPos){ //선택 해제
                setSelected(-1)
                binding.btnSave.text = "+ 여정 새로 만들기"
            }else{ //선택
                setSelected(position)
                binding.btnSave.text = "선택 완료"
            }
            notifyItemChanged(lastPos) // 선택 해제
            notifyItemChanged(position) // 선택
        }
    }
}