package com.app.myfoottrip.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Filter
import com.app.myfoottrip.network.api.UserApi
import com.app.myfoottrip.network.service.BoardService
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private const val TAG = "싸피"

class BoardRepository {

    private val boardService = Application.retrofit.create(BoardService::class.java)
    private val headerBoardService = Application.headerRetrofit.create(BoardService::class.java)

    private val _boardListResponseLiveData = MutableLiveData<NetworkResult<ArrayList<Board>>>()
    val boardListResponseLiveData: LiveData<NetworkResult<ArrayList<Board>>>
        get() = _boardListResponseLiveData

    private val _createResponseLiveData = MutableLiveData<NetworkResult<Board>>()
    val createResponseLiveData: LiveData<NetworkResult<Board>>
        get() = _createResponseLiveData

    private val _likeResponseLiveData = MutableLiveData<NetworkResult<Boolean>>()
    val likeResponseLiveData: LiveData<NetworkResult<Boolean>>
        get() = _likeResponseLiveData

    private val _boardResponseLiveData = MutableLiveData<NetworkResult<Board>>()
    val boardResponseLiveData: LiveData<NetworkResult<Board>>
        get() = _boardResponseLiveData

    private val _writeBoardResponseLiveData = MutableLiveData<NetworkResult<ArrayList<Board>>>()
    val writeBoardResponseLiveData: LiveData<NetworkResult<ArrayList<Board>>>
        get() = _writeBoardResponseLiveData

    private val _likeBoardResponseLiveData = MutableLiveData<NetworkResult<ArrayList<Board>>>()
    val likeBoardResponseLiveData: LiveData<NetworkResult<ArrayList<Board>>>
        get() = _likeBoardResponseLiveData

    private val _updateBoardResponseLiveData = MutableLiveData<NetworkResult<Board>>()
    val updateBoardResponseLiveData: LiveData<NetworkResult<Board>>
        get() = _updateBoardResponseLiveData

    private val _deleteBoardResponseLiveData = MutableLiveData<NetworkResult<String>>()
    val deleteBoardResponseLiveData: LiveData<NetworkResult<String>>
        get() = _deleteBoardResponseLiveData

    // 전체 게시물 조회
    suspend fun getBoardList(){
        var response = boardService.getBoardList()

        // 처음은 Loading 상태로 지정
        _boardListResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _boardListResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _boardListResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _boardListResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

    //게시물 삽입
    suspend fun createBoard(board: Board){
        var response = headerBoardService.writeBoard(board)

        if (response.isSuccessful && response.body() != null) {
            _createResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _createResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _createResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

    //게시물 필터
    suspend fun getFilteredBoardList(filter: Filter){
        var response = boardService.getFilteredBoardList(filter)

        if (response.isSuccessful && response.body() != null) {
            _boardListResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _boardListResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _boardListResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

    //게시물 좋아요
    suspend fun likeBoard(boardId : Int, message: String){
        var response = headerBoardService.likeBoard(boardId,message)

        if (response.isSuccessful && response.body() != null) {
            _likeResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _likeResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _likeResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

    //게시물 조회
    suspend fun getBoard(boardId: Int){
        var response = headerBoardService.getBoard(boardId)

        if (response.isSuccessful && response.body() != null) {
            _boardResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _boardResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _boardResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

    //자신이 작성한 전체 게시물 조회
    suspend fun getWriteBoardList(){
        var response = headerBoardService.getWriteBoardList()

        // 처음은 Loading 상태로 지정
        _writeBoardResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _writeBoardResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _writeBoardResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _writeBoardResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

    //좋아요한 전체 게시물 조회
    suspend fun getLikeBoardList(){
        var response = headerBoardService.getLikeBoardList()

        // 처음은 Loading 상태로 지정
        _likeBoardResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _likeBoardResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _likeBoardResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _likeBoardResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

    //게시물 수정
    suspend fun updateBoard(boardId: Int, board: Board){
        var response = headerBoardService.updateBoard(boardId,board)

        // 처음은 Loading 상태로 지정
        _updateBoardResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _updateBoardResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _updateBoardResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _updateBoardResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

    //게시물 삭제
    suspend fun deleteBoard(boardId: Int){
        var response = headerBoardService.deleteBoard(boardId)

        // 처음은 Loading 상태로 지정
        _deleteBoardResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _deleteBoardResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _deleteBoardResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _deleteBoardResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

} // End of BoardRepository