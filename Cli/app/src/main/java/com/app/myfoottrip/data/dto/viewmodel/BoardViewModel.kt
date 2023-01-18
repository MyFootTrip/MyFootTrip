package com.app.myfoottrip.data.dto.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.repository.BoardRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val TAG = "싸피"

class BoardViewModel : ViewModel() {
    
    // 이메일 사용 여부 체크 값 livedata
    private val _boardList = MutableLiveData<ArrayList<Board>>()
    val boardList: LiveData<ArrayList<Board>>
        get() = _boardList


    // 이메일 중복 체크
    fun getBoardList() {
        val job = viewModelScope.async {
            BoardRepository().getBoardList()
        }

        viewModelScope.launch {
            _boardList.value = BoardRepository().getBoardList()
        }
    } // End of emailUsedCheck


} // End of BoardViewModel
