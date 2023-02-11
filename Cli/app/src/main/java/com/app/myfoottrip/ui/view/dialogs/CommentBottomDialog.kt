package com.app.myfoottrip.ui.view.dialogs

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Comment
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.data.viewmodel.CommentViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.DialogCommentBottomBinding
import com.app.myfoottrip.ui.adapter.CommentAdapter
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showToastMessage
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


private const val TAG = "CommentBottomDialog_마이풋트립"
class CommentBottomDialog(private val listener: OnClickListener,private val commentList: ArrayList<Comment>) :
    BottomSheetDialogFragment(),
    View.OnClickListener {

    val mTag = "댓글 바텀 시트 다이얼로그"

    private lateinit var binding: DialogCommentBottomBinding
    private val boardViewModel by activityViewModels<BoardViewModel>()
    private val userViewModel by activityViewModels<UserViewModel>()
    private val commentViewModel by activityViewModels<CommentViewModel>()

    private lateinit var commentAdapter: CommentAdapter
    private var isWrite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogCommentBottomBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()

        init()

        binding.apply {
            clComment.setOnClickListener { initCommentInput() }
            rvComment.isNestedScrollingEnabled = true
            ivCancelBtn.setOnClickListener {
                dialog?.dismiss()
            }
        }
    }

    interface OnClickListener {
        fun onClick(dialog: CommentBottomDialog)
    }

    override fun onClick(p0: View?) {
        listener.onClick(this)
    }

    private fun init(){
        initProfileImg()
        getBoard()
    }

    private fun initObserver(){
        getBoardObserver()
        writeCommentObserver()
    }

    // 프로필 이미지 불러오기
    private fun initProfileImg(){
        binding.apply {
            if (userViewModel.wholeMyData.value?.join?.profile_image.isNullOrEmpty()){
                ivProfile.setPadding(10)
                Glide.with(this@CommentBottomDialog).load(R.drawable.ic_my).fitCenter().into(ivProfile)
                ivProfile.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white))
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.main)))
            }else {
                Glide.with(this@CommentBottomDialog).load(userViewModel.wholeMyData.value?.join?.profile_image).centerCrop().into(ivProfile)
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
    private fun initCommentAdapter(){

        boardViewModel.board.value?.data!!.commentList.sortByDescending {it.writeDate}
        Log.d(TAG, "테스트: ${boardViewModel.board.value?.data!!.commentList}")
        commentAdapter = CommentAdapter(boardViewModel.board.value?.data!!.commentList,userViewModel.wholeMyData.value!!.uid)

        commentAdapter.setItemClickListener(object : CommentAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, commentId: Int) {
                var popupMenu = PopupMenu(requireContext(),view)

                requireActivity().menuInflater.inflate(R.menu.comment_menu,popupMenu.menu)

                popupMenu.show()
                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.menu_update -> {
                            updateCommentObserver(position)
                            updateCommentInput(commentId,boardViewModel.board.value?.data!!.commentList[position])
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
                val message = "${user?.join?.nickname}님이 ${boardViewModel.board.value?.data?.title}에 댓글이 달렸습니다!"
                val comment = Comment(-1, boardViewModel.boardId,user!!.join.profile_image,user.uid,user.join.nickname,dialog.commentMsg.text.toString(),
                    Date(System.currentTimeMillis()),message)
                writeComment(comment)
                isWrite = true
                dialog.dismiss()
            }
        },userViewModel.wholeMyData.value!!,"")

        inputDialog.show(parentFragmentManager, inputDialog.mTag)
    }

    //댓글 수정 입력창 생성
    private fun updateCommentInput(commentId: Int,commentOrigin: Comment){
        val updateDialog = CommentInputDialog(object : CommentInputDialog.OnClickListener {
            override fun onClick(dialog: CommentInputDialog) {
                val user = userViewModel.wholeMyData.value
                val comment = Comment(commentId, boardViewModel.boardId,user!!.join.profile_image,user.uid,user.join.nickname,dialog.commentMsg.text.toString(),
                    Date(System.currentTimeMillis()),"")
                updateComment(comment)
                dialog.dismiss()
            }
        },userViewModel.wholeMyData.value!!,commentOrigin.content)

        updateDialog.show(parentFragmentManager,updateDialog.mTag)
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
                    boardViewModel.board.value?.data = it.data!!
                    initCommentExist(it.data!!)
                    initCommentAdapter()
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
                    boardViewModel.board.value?.data = it.data!!
                    initCommentExist(it.data!!)
                    initCommentAdapter()
                    commentAdapter.notifyItemInserted(0)
                    binding.rvComment.scrollToPosition(0)
                    if (isWrite){
                        requireContext().showToastMessage("댓글이 등록되었습니다.")
                    }
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
                    boardViewModel.board.value?.data = it.data!!
                    initCommentExist(it.data!!)
                    initCommentAdapter()
                    commentAdapter.notifyItemChanged(position)
                    requireContext().showToastMessage("댓글이 수정되었습니다.")
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
                    boardViewModel.board.value?.data = it.data!!
                    initCommentExist(it.data!!)
                    initCommentAdapter()
                    commentAdapter.notifyItemRemoved(position)
                    requireContext().showToastMessage("댓글이 삭제되었습니다.")
                }
                is NetworkResult.Error -> {}
                is NetworkResult.Loading -> {}
            }
        }
    }
}