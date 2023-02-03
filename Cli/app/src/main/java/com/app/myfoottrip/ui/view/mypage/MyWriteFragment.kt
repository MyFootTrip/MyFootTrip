package com.app.myfoottrip.ui.view.mypage

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.databinding.FragmentMyWriteBinding
import com.app.myfoottrip.ui.adapter.LikeBoardAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyWriteFragment : BaseFragment<FragmentMyWriteBinding>(
    FragmentMyWriteBinding::bind, R.layout.fragment_my_write
) {
    private lateinit var mainActivity: MainActivity

    private val navigationViewModel by activityViewModels<NavigationViewModel>()
    private val boardViewModel by activityViewModels<BoardViewModel>()
    private lateinit var callback: OnBackPressedCallback

    private lateinit var likeBoardAdapter: LikeBoardAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mainActivity = context as MainActivity

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigationViewModel.type = 1
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this@MyWriteFragment, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        binding.apply {
            // 뒤로가기
            ivBack.setOnClickListener {
                navigationViewModel.type = 1
                findNavController().popBackStack()
            }
        }

    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun init(){
        getWriteBoardListObserver()
        getWriteBoardList()
    }

    private fun initWriteBoardAdapter(boardList: ArrayList<Board>) {

        likeBoardAdapter = LikeBoardAdapter(boardList)

        likeBoardAdapter.setItemClickListener(object : LikeBoardAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, boardId: Int) {
                boardViewModel.boardId = boardId
                navigationViewModel.type = 3
                findNavController().navigate(R.id.action_myWriteFragment_to_boardFragment)
            }
        })

        binding.rvMyPageWrite.apply {
            adapter = likeBoardAdapter
            //원래의 목록위치로 돌아오게함
            likeBoardAdapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    //좋아요한 게시물 리스트 조회
    private fun getWriteBoardList(){
        CoroutineScope(Dispatchers.IO).launch {
            boardViewModel.getWriteBoardList()
        }
    }

    private fun getWriteBoardListObserver(){
        boardViewModel.writeList.observe(viewLifecycleOwner){
            when(it){
                is NetworkResult.Success -> {
                    initWriteBoardAdapter(it.data as ArrayList<Board>)
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }
}