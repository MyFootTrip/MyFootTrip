package com.app.myfoottrip.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Alarm
import com.app.myfoottrip.network.service.AlarmService
import com.app.myfoottrip.util.NetworkResult


private const val TAG = "싸피"

class AlarmRepository{

    private val alarmService = Application.retrofit.create(AlarmService::class.java)
    private val headerAlarmService = Application.headerRetrofit.create(AlarmService::class.java)

    private val _alarmListResponseLiveData = MutableLiveData<NetworkResult<ArrayList<Alarm>>>()
    val alarmListResponseLiveData: LiveData<NetworkResult<ArrayList<Alarm>>>
        get() = _alarmListResponseLiveData

//    private val _deleteBoardResponseLiveData = MutableLiveData<NetworkResult<String>>()
//    val deleteBoardResponseLiveData: LiveData<NetworkResult<String>>
//        get() = _deleteBoardResponseLiveData

    // 전체 게시물 조회
    suspend fun getAlarmList(){
        var response = headerAlarmService.getAlarmList()

        // 처음은 Loading 상태로 지정
        _alarmListResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _alarmListResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _alarmListResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _alarmListResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

//    //게시물 삭제
//    suspend fun deleteBoard(boardId: Int){
//        var response = headerBoardService.deleteBoard(boardId)
//
//        // 처음은 Loading 상태로 지정
//        _deleteBoardResponseLiveData.postValue(NetworkResult.Loading())
//
//        if (response.isSuccessful && response.body() != null) {
//            _deleteBoardResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
//        } else if (response.errorBody() != null) {
//            _deleteBoardResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
//        } else {
//            _deleteBoardResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
//        }
//    }

} // End of AlarmRepository.kt