package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Comment
import com.app.myfoottrip.data.repository.CommentRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.launch

private const val TAG = "CommentViewModel_마이풋트립"
class CommentViewModel : ViewModel() {
    private val commentRepository = CommentRepository()

    val createBoard: LiveData<NetworkResult<Board>>
        get() = commentRepository.createResponseLiveData

    val updateBoard: LiveData<NetworkResult<Board>>
        get() = commentRepository.updateResponseLiveData

    val deleteBoard: LiveData<NetworkResult<Board>>
        get() = commentRepository.deleteResponseLiveData

    // 댓글 생성
    fun writeComment(boardId: Int, comment: Comment) {
        viewModelScope.launch {
            commentRepository.writeComment(boardId,comment)
        }
    }

    //댓글 수정
    fun updateComment(boardId: Int,commentId: Int, comment: Comment){
        viewModelScope.launch {
            commentRepository.updateComment(boardId, commentId, comment)
        }
    }

    //댓글 삭제
    fun deleteComment(boardId: Int, commentId: Int){
        viewModelScope.launch {
            commentRepository.deleteComment(boardId, commentId)
        }
    }
}