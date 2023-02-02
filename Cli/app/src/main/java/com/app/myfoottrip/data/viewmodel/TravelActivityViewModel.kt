package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.repository.TravelRepository
import com.app.myfoottrip.util.NetworkResult

class TravelActivityViewModel : ViewModel() {
    private val travelRepository = TravelRepository()

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
