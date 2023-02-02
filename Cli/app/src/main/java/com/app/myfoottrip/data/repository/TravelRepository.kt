package com.app.myfoottrip.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.network.api.TravelApi
import com.app.myfoottrip.util.NetworkResult

private const val TAG = "TravelRepository_싸피"

class TravelRepository {
    private val travelApi = Application.retrofit.create(TravelApi::class.java)

    // 헤더가 담긴
    private val travelHeaderApi = Application.headerRetrofit.create(TravelApi::class.java)


    //유저 여정 값
    private val _travelListResponseLiveData = MutableLiveData<NetworkResult<ArrayList<Travel>>>()
    val travelListResponseLiveData: LiveData<NetworkResult<ArrayList<Travel>>>
        get() = _travelListResponseLiveData

    // 여행 추가
    private val _createTravelResponseLiveData = MutableLiveData<NetworkResult<Int>>()
    val createTravelResponseLiveData: LiveData<NetworkResult<Int>>
        get() = _createTravelResponseLiveData


    // 유저 여행 데이터 조회
    private val _getUserTravelDataResponseLiveData = MutableLiveData<NetworkResult<Travel>>()
    val getUserTravelDataResponseLiveData: LiveData<NetworkResult<Travel>>
        get() = _getUserTravelDataResponseLiveData


    // 여행 데이터 추가
    suspend fun createTravel(travel: Travel) {
        val response = travelHeaderApi.createTravel(travel)
        _createTravelResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful) {
            _createTravelResponseLiveData.postValue(NetworkResult.Success(response.code()))
        } else if (response.errorBody() != null) {
            _createTravelResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _createTravelResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of createTravel

    fun setCreateTravelResponseLiveData() {
        _createTravelResponseLiveData.postValue(null)
    } // End of setCreateTravelResponseLiveData


    // 각 유저별 여행 기록 정보를 가져옴
    suspend fun getUserTravel(userId: Int) {
        val response = travelApi.getUserTravel(userId)

        // 처음은 Loading 상태로 지정
        _travelListResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _travelListResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _travelListResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _travelListResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of getUserTravel

    //여정 조회
    suspend fun getUserTravelData(travelId: Int) {
        val response = travelHeaderApi.getTravel(travelId)

        // 처음은 Loading 상태로 지정
        _getUserTravelDataResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _getUserTravelDataResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _getUserTravelDataResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _getUserTravelDataResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of getTravel


    // 여행 데이터 수정 response값 LiveData
    private val _userTravelDataUpdateResponseLiveData = MutableLiveData<NetworkResult<Travel>>()
    val userTravelDataUpdateResponseLiveData: LiveData<NetworkResult<Travel>>
        get() = _userTravelDataUpdateResponseLiveData

    suspend fun userTravelDataUpdate(travelId: Int, updateTravelData: Travel) {
        val response = travelHeaderApi.updateTravel(travelId, updateTravelData)

        // 처음은 Loading 상태로 지정
        _userTravelDataUpdateResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _userTravelDataUpdateResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _userTravelDataUpdateResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _userTravelDataUpdateResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of userTravelDataUpdate

} // End of TravelRepository class
