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

    // 게시물 전체 조회
    fun getAlarmList() {
        viewModelScope.launch {
            alarmRepository.getAlarmList()
        }
    }
}