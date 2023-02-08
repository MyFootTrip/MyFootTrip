package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Alarm
import com.app.myfoottrip.data.repository.AlarmRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.launch

class AlarmViewModel: ViewModel() {

    private val alarmRepository = AlarmRepository()

    // 알람 전체 리스트
    val alarmList: LiveData<NetworkResult<ArrayList<Alarm>>>
        get() = alarmRepository.alarmListResponseLiveData

    val alarmCount: LiveData<NetworkResult<Int>>
        get() = alarmRepository.alarmCountResponseLiveData

    val alarmDeleteResponseLiveData: LiveData<NetworkResult<Int>>
        get() = alarmRepository.alarmDeleteResponseLiveData

    // 알림 리스트 전체 조회
    fun getAlarmList() {
        viewModelScope.launch {
            alarmRepository.getAlarmList()
        }
    }

    //알림 카운트
    fun getAlarmCount(){
        viewModelScope.launch {
            alarmRepository.getAlarmCount()
        }
    }

    // 알림 데이터 삭제
    suspend fun alarmDelete(notificationId: Int) {
        viewModelScope.launch {
            alarmRepository.alarmDelete(notificationId)
        }
    }
}