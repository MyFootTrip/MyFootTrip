package com.app.myfoottrip.ui.view.board

import android.app.Activity.RESULT_OK
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Comment
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.databinding.FragmentCommentBinding
import com.app.myfoottrip.ui.adapter.CommentAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.CommentInputDialog
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.GalleryUtils
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.*
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
            clComment.setOnClickListener { initCommentInput() }
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

    //댓글 입력창 생성
    private fun initCommentInput(){
        val inputDialog = CommentInputDialog(object : CommentInputDialog.OnClickListener {
            override fun onClick(dialog: CommentInputDialog) {
                showToast("${dialog.comment.text}", ToastType.SUCCESS, false)
                dialog.dismiss()
            }
        })

        inputDialog.show(parentFragmentManager, inputDialog.mTag)
    }


}