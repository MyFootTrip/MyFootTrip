package com.app.myfoottrip.ui.view.board

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout.VERTICAL
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Comment
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.databinding.FragmentCommentBinding
import com.app.myfoottrip.ui.adapter.CommentAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.NetworkResult
import com.forms.sti.progresslitieigb.finishLoadingIGB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "CommentFragment_마이풋트립"
class CommentFragment : BaseFragment<FragmentCommentBinding>(
    FragmentCommentBinding::bind, R.layout.fragment_comment
) {
    private lateinit var mainActivity: MainActivity

    private val boardViewModel by activityViewModels<BoardViewModel>()

    private lateinit var commentList: ArrayList<Comment>
    private lateinit var commentAdapter: CommentAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        binding.apply {
            ivBack.setOnClickListener {findNavController().popBackStack()} //뒤로가기
            createBtn.setOnClickListener {
                val board = Board(9999,1,"테스트계정","",Date(System.currentTimeMillis()),"혼자놀기","임시제목입니다"
                ,"임시 내용입니다.", arrayListOf(), null,2,2)
                createBoard(board)
                createBoardObserver()
            }
        }
    }

    private fun init(){
        initCommentExist()
        initCommentAdapter()
    }

    // 댓글이 없을 시 보여주는 뷰
    private fun initCommentExist(){
        if (boardViewModel.board.commentCount == 0) binding.tvCommentExist.visibility = View.VISIBLE
        else binding.tvCommentExist.visibility = View.INVISIBLE
    }

    //댓글 리사이클러 뷰 생성
    private fun initCommentAdapter(){
        commentList = ArrayList()

        val comment = arrayOf(
            Comment(1,1,"",1,"테스트","테스트 내용",Date(System.currentTimeMillis())),
            Comment(2,1,"",1,"테스트2","테스트 내용2",Date(System.currentTimeMillis())),
            Comment(3,1,"",1,"테스트3","테스트 내용3",Date(System.currentTimeMillis())),
            Comment(4,1,"",1,"테스트4","테스트 내용4",Date(System.currentTimeMillis())),
            Comment(5,1,"",1,"테스트5","테스트 내용5",Date(System.currentTimeMillis()))
        )
        
        commentList.addAll(comment)
        
        commentAdapter = CommentAdapter(commentList)

        commentAdapter.setItemClickListener(object : CommentAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, commentId: Int) {
                Log.d(TAG, "onClick: $commentId")
            }

        })

        binding.rvComment.apply {
            adapter = commentAdapter
        }
    }

    // ----------------Retrofit------------------
    //게시물 전체 받아오기
    private fun createBoard(board : Board){
        CoroutineScope(Dispatchers.IO).launch {
            boardViewModel.createBoard(board)
        }
    }

    private fun createBoardObserver() {
        boardViewModel.isCreated.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "createBoardObserver: ${it.data}")
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "게시물 조회 Error: ${it.data}")
                }
                is NetworkResult.Loading -> {

                }
            }
        }
    } // 게시물 전체 받아오기
}