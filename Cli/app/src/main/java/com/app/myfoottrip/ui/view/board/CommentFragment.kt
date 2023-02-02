package com.app.myfoottrip.ui.view.board

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Comment
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.data.viewmodel.CommentViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentCommentBinding
import com.app.myfoottrip.ui.adapter.CommentAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.CommentInputDialog
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import com.bumptech.glide.Glide
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
    private val userViewModel by activityViewModels<UserViewModel>()
    private val commentViewModel by activityViewModels<CommentViewModel>()

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
        initProfileImg()
        getBoardObserver()
        getBoard()
    }

    // 프로필 이미지 불러오기
    private fun initProfileImg(){
        binding.apply {
            if (userViewModel.wholeMyData.value?.join?.profile_image.isNullOrEmpty()){
                ivProfile.setPadding(10)
                Glide.with(this@CommentFragment).load(R.drawable.ic_my).fitCenter().into(ivProfile)
                ivProfile.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white))
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.main)))
            }else {
                Glide.with(this@CommentFragment).load(userViewModel.wholeMyData.value?.join?.profile_image).centerCrop().into(ivProfile)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white)))
            }
        }
    }

    // 댓글이 없을 시 보여주는 뷰
    private fun initCommentExist(board : Board){
        if (board.commentList.size == 0) binding.tvCommentExist.visibility = View.VISIBLE
        else binding.tvCommentExist.visibility = View.INVISIBLE
    }


    //댓글 리사이클러 뷰 생성
    private fun initCommentAdapter(commentList: ArrayList<Comment>){

        commentList.sortByDescending {it.writeDate}

        commentAdapter = CommentAdapter(commentList,userViewModel.wholeMyData.value!!.uid)

        commentAdapter.setItemClickListener(object : CommentAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, commentId: Int) {
                var popupMenu = PopupMenu(requireContext(),view)

                mainActivity.menuInflater.inflate(R.menu.comment_menu,popupMenu.menu)

                popupMenu.show()
                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.menu_update -> {
                            updateCommentObserver(position)
                            updateCommentInput(commentId)
                            return@setOnMenuItemClickListener true
                        }
                        R.id.menu_delete -> {
                            deleteCommentObserver(position)
                            deleteComment(commentId)
                            return@setOnMenuItemClickListener true
                        }else ->{
                            return@setOnMenuItemClickListener true
                        }
                    }
                }
            }

        })

        binding.rvComment.apply {
            adapter = commentAdapter
        }
    }

    //댓글 생성 입력창 생성
    private fun initCommentInput(){
        val inputDialog = CommentInputDialog(object : CommentInputDialog.OnClickListener {
            override fun onClick(dialog: CommentInputDialog) {
                val user = userViewModel.wholeMyData.value
                val message = "${user?.join?.nickname}님이 ${boardViewModel.board.value?.data?.title}에 댓글이 달렸습니다!\uD83D\uDCAC"
                val comment = Comment(-1, boardViewModel.boardId,user!!.join.profile_image,user.uid,user.join.nickname,dialog.commentMsg.text.toString(),Date(System.currentTimeMillis()),message)
                writeCommentObserver()
                writeComment(comment)
                dialog.dismiss()
            }
        },userViewModel.wholeMyData.value!!)

        inputDialog.show(parentFragmentManager, inputDialog.mTag)
    }

    //댓글 수정 입력창 생성
    private fun updateCommentInput(commentId: Int){
        val updateDialog = CommentInputDialog(object : CommentInputDialog.OnClickListener {
            override fun onClick(dialog: CommentInputDialog) {
                val user = userViewModel.wholeMyData.value
                val comment = Comment(commentId, boardViewModel.boardId,user!!.join.profile_image,user.uid,user.join.nickname,dialog.commentMsg.text.toString(),Date(System.currentTimeMillis()),"")
                updateComment(comment)
                dialog.dismiss()
            }
        },userViewModel.wholeMyData.value!!)

        updateDialog.show(parentFragmentManager, updateDialog.mTag)
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
                    initCommentExist(it.data!!)
                    initCommentAdapter(it.data!!.commentList)
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }

    //댓글 생성
    private fun writeComment(comment: Comment) {
        CoroutineScope(Dispatchers.IO).launch {
            commentViewModel.writeComment(boardViewModel.boardId,comment)
        }
    }

    private fun writeCommentObserver() {
        commentViewModel.createBoard.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    initCommentExist(it.data!!)
                    initCommentAdapter(it.data!!.commentList)
                    commentAdapter.notifyItemInserted(0)
                    binding.rvComment.scrollToPosition(0)
                    binding.root.showSnackBarMessage("댓글이 등록되었습니다.")
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "writeCommentObserver: 통신에러")
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }

    //댓글 수정
    private fun updateComment(comment: Comment) {
        CoroutineScope(Dispatchers.IO).launch {
            commentViewModel.updateComment(boardViewModel.boardId,comment.commentId,comment)
        }
    }

    private fun updateCommentObserver(position: Int) {
        commentViewModel.updateBoard.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    initCommentExist(it.data!!)
                    initCommentAdapter(it.data!!.commentList)
                    commentAdapter.notifyItemChanged(position)
                    binding.root.showSnackBarMessage("댓글이 수정되었습니다.")
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }

    //댓글 삭제
    private fun deleteComment(commentId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            commentViewModel.deleteComment(boardViewModel.boardId,commentId)
        }
    }

    private fun deleteCommentObserver(position: Int) {
        commentViewModel.deleteBoard.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    initCommentExist(it.data!!)
                    initCommentAdapter(it.data!!.commentList)
                    commentAdapter.notifyItemRemoved(position)
                    binding.root.showSnackBarMessage("댓글이 삭제되었습니다.")
                }
                is NetworkResult.Error -> {}
                is NetworkResult.Loading -> {}
            }
        }
    }
}