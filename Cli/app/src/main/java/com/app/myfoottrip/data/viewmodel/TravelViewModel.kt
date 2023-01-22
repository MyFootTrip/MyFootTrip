package com.app.myfoottrip.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.repository.TravelRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.launch


private const val TAG = "TravelViewModel_싸피"

class TravelViewModel : ViewModel() {
    private val travelRepository = TravelRepository()

    // 선택된 지역 리스트
    private var locationList = arrayListOf<String>()
    var selectedtravel: Travel? = null

    //여정 조회 값
    private val _travelData = MutableLiveData<Travel>()
    val travelData: LiveData<Travel>
        get() = _travelData

    //유저별 여정 조회 값
    val travelUserData: LiveData<NetworkResult<ArrayList<Travel>>>
        get() = travelRepository.travelListResponseLiveData


    //여정 기록 state
    private val _travelResponseStatus = MutableLiveData(false)
    val travelResponseStatus: LiveData<Boolean>
        get() = _travelResponseStatus

    fun setLocationList(list: ArrayList<String>) {
        locationList = list
    }

    //유저별 여정 확인
    suspend fun getUserTravel(userId: Int) {
        viewModelScope.launch {
            Log.d(TAG, " getUserTravel 들어가기 전: ${travelUserData.value}")
            travelRepository.getUserTravel(userId)
            Log.d(TAG, "getUserTravel 나옴 : ${travelUserData.value}")
        }
    }

    //여정 조회
    fun getTravel(travelId: Int) {
        var travelData: Travel?
        viewModelScope.launch {
            travelData = TravelRepository().getTravel(travelId)
            if (travelData != null) {
                _travelData.postValue(travelData)
            }
        }
    }

    //여정 추가
    fun makeTravel(travel: Travel) {
        viewModelScope.launch {
            TravelRepository().sendTravel(travel)
        }
    }

    //여정 수정

}