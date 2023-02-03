package com.app.myfoottrip.ui.view.mypage

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.databinding.FragmentMyLikeBinding
import com.app.myfoottrip.ui.adapter.LikeBoardAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MyLikeFragment_마이풋트립"
class MyLikeFragment : BaseFragment<FragmentMyLikeBinding>(
    FragmentMyLikeBinding::bind, R.layout.fragment_my_like
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
        requireActivity().onBackPressedDispatcher.addCallback(this@MyLikeFragment, callback)
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
        getLikeBoardListObserver()
        getLikeBoardList()
    }

    private fun initLikeBoardAdapter(boardList: ArrayList<Board>) {

        likeBoardAdapter = LikeBoardAdapter(boardList)

        likeBoardAdapter.setItemClickListener(object : LikeBoardAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, boardId: Int) {
                boardViewModel.boardId = boardId
                navigationViewModel.type = 3
                findNavController().navigate(R.id.action_myLikeFragment_to_boardFragment)
            }
        })

        binding.rvMyPageLike.apply {
            adapter = likeBoardAdapter
            //원래의 목록위치로 돌아오게함
            likeBoardAdapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    //좋아요한 게시물 리스트 조회
    private fun getLikeBoardList(){
        CoroutineScope(Dispatchers.IO).launch {
            boardViewModel.getLikeBoardList()
        }
    }

    private fun getLikeBoardListObserver(){
        boardViewModel.likeList.observe(viewLifecycleOwner){
            when(it){
                is NetworkResult.Success -> {
                    initLikeBoardAdapter(it.data as ArrayList<Board>)
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }
}