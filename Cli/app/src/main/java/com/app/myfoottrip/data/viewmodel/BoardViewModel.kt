package com.app.myfoottrip.data.viewmodel


import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Filter
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.repository.BoardRepository
import com.app.myfoottrip.network.api.UserApi
import com.app.myfoottrip.network.service.BoardService
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "싸피"

class BoardViewModel : ViewModel() {

    private val boardRepository = BoardRepository()
    /* data */
    private val filterData : MutableLiveData<Filter> = MutableLiveData()

    // 라이브 데이터 변경 시 다른 라이브 데이터 발행
    val boardList = filterData.switchMap { queryString ->
        boardRepository.getBoardList(queryString).cachedIn(viewModelScope)
    }

    var boardId : Int = -1

    val board: LiveData<NetworkResult<Board>>
        get() = boardRepository.boardResponseLiveData

    val isCreated: LiveData<NetworkResult<Board>>
        get() = boardRepository.createResponseLiveData

    val likeCheck : LiveData<NetworkResult<Boolean>>
        get() = boardRepository.likeResponseLiveData

    val writeList : LiveData<NetworkResult<ArrayList<Board>>>
        get() = boardRepository.writeBoardResponseLiveData

    val likeList : LiveData<NetworkResult<ArrayList<Board>>>
        get() = boardRepository.likeBoardResponseLiveData

    val updateBoard : LiveData<NetworkResult<Board>>
        get() = boardRepository.updateBoardResponseLiveData

    val deleteBoard : LiveData<NetworkResult<String>>
        get() = boardRepository.deleteBoardResponseLiveData

    //필터 변경
    fun getBoardList(value : Filter) {
        filterData.value = value
    }

    // 게시물 생성
    fun createBoard(board: Board) {
        viewModelScope.launch {
            boardRepository.createBoard(board)
        }
    }

    //게시물 좋아요
    fun likeBoard(boardId: Int, message: String){
        viewModelScope.launch {
            boardRepository.likeBoard(boardId,message)
        }
    }

    //게시물 조회
    fun getBoard(boardId: Int){
        viewModelScope.launch {
            boardRepository.getBoard(boardId)
        }
    }

    //게시물 수정
    fun updateBoard(boardId: Int, board: Board){
        viewModelScope.launch {
            boardRepository.updateBoard(boardId,board)
        }
    }

    //게시물 삭제
    fun deleteBoard(boardId: Int){
        viewModelScope.launch {
            boardRepository.deleteBoard(boardId)
        }
    }

    //내가 작성한 전체 게시물 조회
    fun getWriteBoardList(){
        viewModelScope.launch {
            boardRepository.getWriteBoardList()
        }
    }

    //내가 좋아요한 전체 게시물 조회
    fun getLikeBoardList(){
        viewModelScope.launch {
            boardRepository.getLikeBoardList()
        }
    }
} // End of BoardViewModel
