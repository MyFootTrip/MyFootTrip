package com.app.myfoottrip.data.dto.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.repository.BoardRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "싸피"

class BoardViewModel : ViewModel() {

    private val boardRepository = BoardRepository()

    // 이메일 사용 여부 체크 값 livedata
    val boardList: LiveData<NetworkResult<ArrayList<Board>>>
        get() = boardRepository.boardListResponseLiveData


    // 이메일 중복 체크
    fun getBoardList() {
        viewModelScope.launch {
            boardRepository.getBoardList()
        }
    } // End of emailUsedCheck


} // End of BoardViewModel
