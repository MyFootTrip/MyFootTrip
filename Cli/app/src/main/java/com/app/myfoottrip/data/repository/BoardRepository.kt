package com.app.myfoottrip.data.repository

import android.util.Log
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.network.api.UserApi
import com.app.myfoottrip.network.service.BoardService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private const val TAG = "싸피"

class BoardRepository {
    private val boardService = Application.retrofit.create(BoardService::class.java)

    // 이메일 중복 체크
    suspend fun getBoardList(): ArrayList<Board> {
        var boardList = arrayListOf<Board>()

        withContext(Dispatchers.IO) {
            var response = boardService.getBoardList()
            Log.d(TAG, "checkEmailId: $response")

            if (response.isSuccessful) {
                boardList = response.body() as ArrayList<Board>
            } else {
                Log.d(TAG, "response 실패 값: ${response.errorBody()}")
            }
        }

        return boardList
    } // End of checkEmailId
} // End of BoardRepository