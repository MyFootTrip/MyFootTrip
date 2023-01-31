package com.app.myfoottrip.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Comment
import com.app.myfoottrip.network.service.CommentService
import com.app.myfoottrip.util.NetworkResult

private const val TAG = "CommentRepository_마이풋트립"
class CommentRepository {
    private val commentService = Application.retrofit.create(CommentService::class.java)
    private val headerCommentService = Application.headerRetrofit.create(CommentService::class.java)

    private val _createResponseLiveData = MutableLiveData<NetworkResult<Board>>()
    val createResponseLiveData: LiveData<NetworkResult<Board>>
        get() = _createResponseLiveData

    private val _updateResponseLiveData = MutableLiveData<NetworkResult<Board>>()
    val updateResponseLiveData: LiveData<NetworkResult<Board>>
        get() = _updateResponseLiveData

    private val _deleteResponseLiveData = MutableLiveData<NetworkResult<Board>>()
    val deleteResponseLiveData: LiveData<NetworkResult<Board>>
        get() = _deleteResponseLiveData


    //게시물 삽입
    suspend fun writeComment(boardId: Int, comment: Comment){
        var response = headerCommentService.writeComment(boardId,comment)
        if (response.isSuccessful && response.body() != null) {
            _createResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _createResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _createResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

    //게시물 수정
    suspend fun updateComment(boardId: Int, commentId: Int, comment: Comment){
        var response = headerCommentService.updateComment(boardId,commentId,comment)
        if (response.isSuccessful && response.body() != null) {
            _updateResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _updateResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _updateResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

    //게시물 삭제
    suspend fun deleteComment(boardId: Int, commentId: Int){
        var response = headerCommentService.deleteComment(boardId,commentId)
        if (response.isSuccessful && response.body() != null) {
            _deleteResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _deleteResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _deleteResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }
}