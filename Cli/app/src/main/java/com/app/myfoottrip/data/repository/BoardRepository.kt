package com.app.myfoottrip.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.network.api.UserApi
import com.app.myfoottrip.network.service.BoardService
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private const val TAG = "싸피"

class BoardRepository {
    private val boardService = Application.retrofit.create(BoardService::class.java)

    private val _boardListResponseLiveData = MutableLiveData<NetworkResult<ArrayList<Board>>>()
    val boardListResponseLiveData: LiveData<NetworkResult<ArrayList<Board>>>
        get() = _boardListResponseLiveData

    // 이메일 중복 체크
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
} // End of BoardRepository