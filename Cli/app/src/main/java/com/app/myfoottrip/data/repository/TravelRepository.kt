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
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

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
    } // End of getTravel
} // End of TravelRepository class
