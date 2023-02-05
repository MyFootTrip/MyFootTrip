package com.app.myfoottrip.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Coordinates
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.repository.TravelRepository
import com.app.myfoottrip.ui.view.travel.EventBus
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext


private const val TAG = "TravelActivityViewModel_싸피"
class TravelActivityViewModel : ViewModel() {
    private val travelRepository = TravelRepository()

    // 가장 최근에 찍힌 좌표값
    private val _recentCoor = MutableLiveData<Coordinates>()
    val recentCoor: LiveData<Coordinates>
        get() = _recentCoor

    fun setRecentCoor(newCoordinates: Coordinates) {
        _recentCoor.postValue(newCoordinates)
    } // End of setRecentCoor

    init {
        viewModelScope.launch {
            EventBus.subscribe<Coordinates>().collect {
                    value ->
                Log.d(TAG, "eventBus: $value")
                _recentCoor.postValue(value)
            }
        }
    }

    // 선택된 지역 리스트
    private val _locationList = ArrayList<String>(emptyList())
    val locationList: ArrayList<String>
        get() = _locationList

    var selectedtravel: Travel? = null

    //여정 조회 값
    private val _travelData = MutableLiveData<Travel>()
    val travelData: LiveData<Travel>
        get() = _travelData


    // 여행정보를 새로만드는지, 기존의 데이터를 불러오는 뷰인지 구분하기 위한 LiveData
    private val _userTravelDataNewOrUpdateCheck = MutableLiveData<Boolean?>(null)
    val userTravelDataNewOrUpdateCheck: MutableLiveData<Boolean?>
        get() = _userTravelDataNewOrUpdateCheck

    // 기존의 여행 정보를 불러와서 저장함
    private val _userTravelData = MutableLiveData<Travel>()
    val userTravelData: LiveData<Travel>
        get() = _userTravelData

    fun setLocationList(list: ArrayList<String>) {
        _locationList.clear()
        _locationList.addAll(list)
    } // End of setLocationList

    fun setUserTravelDataNewOrUpdateCheck(flag: Boolean?) {
        userTravelDataNewOrUpdateCheck.value = flag
    } // End of setUserTravelDataNewOrUpdateCheck

    fun setGetUserTravelData(travelData: Travel) {
        _userTravelData.value = travelData
    } // End of setGetUserTravelData
} // End of TravelActivityViewModel
