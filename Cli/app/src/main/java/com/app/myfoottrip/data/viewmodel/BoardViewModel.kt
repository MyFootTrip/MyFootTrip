package com.app.myfoottrip.data.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.repository.BoardRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "싸피"

class BoardViewModel : ViewModel() {

    private val boardRepository = BoardRepository()

    // 게시물 전체 리스트
    val boardList: LiveData<NetworkResult<ArrayList<Board>>>
        get() = boardRepository.boardListResponseLiveData

    var board = Board(
        -1, -1, "", "", Date(0), "","", "", arrayListOf(), Travel(
            1, 1, arrayListOf(), Date(0), Date(0), arrayListOf(),
        ), 0, 0
    )

    val isCreated : LiveData<NetworkResult<Board>>
        get() = boardRepository.createResponseLiveData


    // 게시물 전체 조회
    fun getBoardList() {
        viewModelScope.launch {
            boardRepository.getBoardList()
        }
    }

    // 게시물 생성
    fun createBoard(board: Board){
        viewModelScope.launch {
            boardRepository.createBoard(board)
        }
    }



} // End of BoardViewModel
