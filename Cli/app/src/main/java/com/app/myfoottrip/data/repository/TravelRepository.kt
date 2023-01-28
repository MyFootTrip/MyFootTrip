package com.app.myfoottrip.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.network.api.TravelApi
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "TravelRepository_싸피"

class TravelRepository {
    private val travelApi = Application.retrofit.create(TravelApi::class.java)

    //여정 생성 값
    private val _travelResponseLiveData = MutableLiveData<NetworkResult<Boolean>>()
    val travelResponseLiveData: LiveData<NetworkResult<Boolean>>
        get() = _travelResponseLiveData

    //유저 여정 값
    private val _travelListResponseLiveData = MutableLiveData<NetworkResult<ArrayList<Travel>>>()
    val travelListResponseLiveData: LiveData<NetworkResult<ArrayList<Travel>>>
        get() = _travelListResponseLiveData

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
    }

    //여정 조회
    suspend fun getTravel(travelId: Int): Travel? {
        var result: Travel? = null
        withContext(Dispatchers.IO) {
            try {
                val response = travelApi.getTravel(travelId)
                if (response.isSuccessful) {
                    result = response.body() as Travel
                } else {
                    Log.d(TAG, "getTravel: response 실패 ${response.errorBody().toString()}")
                }
            } catch (e: Exception) {
                Log.d(TAG, "getTravel: $e")
            }
        }

        return result
    }

    //여정 생성
    suspend fun sendTravel(travel: Travel) {
        val response = travelApi.makeTravel(travel)

        _travelResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            Log.d(TAG, "checkEmailValidateText: 여기로 들어가나요?")
            _travelResponseLiveData.postValue(NetworkResult.Success(true))
        } else if (response.errorBody() != null) {
            _travelResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _travelResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    }

    //여정 수정
}
