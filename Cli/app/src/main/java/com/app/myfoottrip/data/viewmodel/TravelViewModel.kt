package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.repository.TravelRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val TAG = "TravelViewModel_싸피"

class TravelViewModel : ViewModel() {
    private val travelRepository = TravelRepository()

    // 선택된 지역 리스트
    private val _selectLocationList = MutableLiveData<List<String>>(emptyList())
    val selectLocationList: LiveData<List<String>>
        get() = _selectLocationList


    private var locationList = ArrayList<String>(emptyList())
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

    fun setLocationList(list: List<String>) {
        locationList = list as ArrayList<String>
    } // End of setLocationList



    //유저별 여정 확인
    suspend fun getUserTravel(userId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                travelRepository.getUserTravel(userId)
            }
        }
    } // End of getUserTravel

    //여정 조회
    fun getTravel(travelId: Int) {
        var travelData: Travel?
        viewModelScope.launch {
            travelData = TravelRepository().getTravel(travelId)
            if (travelData != null) {
                _travelData.postValue(travelData)
            }
        }
    } // End of getTravel

    //여정 추가
    fun makeTravel(travel: Travel) {
        viewModelScope.launch {
            TravelRepository().sendTravel(travel)
        }
    } // End of makeTravel
} // End of TraveViewModel class
