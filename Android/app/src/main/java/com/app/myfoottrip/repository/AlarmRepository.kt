package com.app.myfoottrip.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.model.Alarm
import com.app.myfoottrip.api.AlarmApi
import com.app.myfoottrip.util.NetworkResult


private const val TAG = "싸피"

class AlarmRepository{

    private val alarmService = Application.retrofit.create(AlarmApi::class.java)
    private val headerAlarmService = Application.headerRetrofit.create(AlarmApi::class.java)

    private val _alarmListResponseLiveData = MutableLiveData<NetworkResult<ArrayList<Alarm>>>()
    val alarmListResponseLiveData: LiveData<NetworkResult<ArrayList<Alarm>>>
        get() = _alarmListResponseLiveData

    private val _alarmDeleteResponseLiveData = MutableLiveData<NetworkResult<Int>>()
    val alarmDeleteResponseLiveData: LiveData<NetworkResult<Int>>
        get() = _alarmDeleteResponseLiveData

    private val _alarmCountResponseLiveData = MutableLiveData<NetworkResult<Int>>()
    val alarmCountResponseLiveData: LiveData<NetworkResult<Int>>
        get() = _alarmCountResponseLiveData

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

    // 전체 게시물 조회
    suspend fun getAlarmCount(){
        var response = headerAlarmService.getAlarmCount()

        // 처음은 Loading 상태로 지정
        _alarmCountResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _alarmCountResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _alarmCountResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _alarmCountResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

    suspend fun alarmDelete(notificationId: Int) {
        val response = headerAlarmService.deleteAlarm(notificationId)

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