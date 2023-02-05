package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Coordinates
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.repository.TravelRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "TravelViewModel_싸피"

class TravelViewModel : ViewModel() {

//    init {
//        viewModelScope.launch {
//            EventBus.subscribe<Coordinates>().collect {
//                value ->
//                Log.d(TAG, "eventBus: $value")
//                _recentCoor.value = (value)
//            }
//        }
//    }

    private val travelRepository = TravelRepository()
    var selectedtravel: Travel? = null

    // 가장 최근에 찍힌 좌표값
    private val _recentCoor = MutableLiveData<Coordinates>()
    val recentCoor: LiveData<Coordinates>
        get() = _recentCoor

    var travelList : ArrayList<Travel> = arrayListOf()

    fun setRecentCoor(newCoordinates: Coordinates) {
        _recentCoor.postValue(newCoordinates)
    } // End of setRecentCoor

    //여정 조회 값
    private val _travelData = MutableLiveData<Travel>()
    val travelData: LiveData<Travel>
        get() = _travelData


    //유저별 여정 조회 값
    val travelUserData: LiveData<NetworkResult<ArrayList<Travel>>>
        get() = travelRepository.travelListResponseLiveData


    // 여행정보를 새로만드는지, 기존의 데이터를 불러오는 뷰인지 구분하기 위한 LiveData
    private val _userTravelDataNewOrUpdateCheck = MutableLiveData<Boolean?>(null)
    val userTravelDataNewOrUpdateCheck: MutableLiveData<Boolean?>
        get() = _userTravelDataNewOrUpdateCheck

    fun setCreateTravelResponseLiveData() {
        travelRepository.setCreateTravelResponseLiveData()
    } // End of setCreateTravelResponseLiveData

    //유저별 여정 확인
    suspend fun getUserTravel(userId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                travelRepository.getUserTravel(userId)
            }
        }
    } // End of getUserTravel

    fun setUserTravelDataNewOrUpdateCheck(flag: Boolean?) {
        userTravelDataNewOrUpdateCheck.value = flag
    } // End of setUserTravelDataNewOrUpdateCheck




    // ======================== 유저 여행 데이터 조회 ========================
    // 기존의 여행 데이터를 가져오는 response 값
    val getUserTravelDataResponseLiveData: LiveData<NetworkResult<Travel>>
        get() = travelRepository.getUserTravelDataResponseLiveData
    
    // 유저의 여행 데이터 조회
    fun getUserTravelData(travelId: Int) {
        viewModelScope.launch {
            travelRepository.getUserTravelData(travelId)
        }
    } // End of getUserTravelData

    // ======================== 유저 여행 데이터 추가 ========================
    // 유저 여행 데이터 추가 response값 LiveData
    val createTravelResponseLiveData: LiveData<NetworkResult<Int>>
        get() = travelRepository.createTravelResponseLiveData

    fun createTravel(travel: Travel) {
        viewModelScope.launch {
            travelRepository.createTravel(travel)
        }
    } // End of createTravel

    // ======================== 유저 여행 데이터 수정 ========================
    // 여행 데이터 수정 response값 LiveData
    val userTravelDataUpdateResponseLiveData: LiveData<NetworkResult<Travel>>
        get() = travelRepository.userTravelDataUpdateResponseLiveData

    // 여행 데이터 수정
    suspend fun userTravelDataUpdate(travelId: Int, updateTravelData: Travel) {
        viewModelScope.launch {
            travelRepository.userTravelDataUpdate(travelId, updateTravelData)
        }
    } // End of userTravelDataUpdate
} // End of TravelViewModel class
