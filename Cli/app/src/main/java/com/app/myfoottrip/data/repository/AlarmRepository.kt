package com.app.myfoottrip.data.repository

import android.util.Log
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

    private val _alarmDeleteResponseLiveData = MutableLiveData<NetworkResult<Int>>()
    val alarmDeleteResponseLiveData: LiveData<NetworkResult<Int>>
        get() = _alarmDeleteResponseLiveData

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

    suspend fun alarmDelete(notificationId: Int) {
        val response = headerAlarmService.deleteAlarm(notificationId)

        Log.d(TAG, "userTravelDataDelete: $response")
        Log.d(TAG, "userTravelDataDelete: ${response.body()}")
        Log.d(TAG, "userTravelDataDelete: ${response.message()}")

        // 처음은 Loading 상태로 지정
        _alarmDeleteResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful) {
            _alarmDeleteResponseLiveData.postValue(NetworkResult.Success(response.code()))
        } else if (response.errorBody() != null) {
            _alarmDeleteResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _alarmDeleteResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of userTravelDataDelete

} // End of AlarmRepository.kt