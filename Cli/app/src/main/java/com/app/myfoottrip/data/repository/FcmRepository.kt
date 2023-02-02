package com.app.myfoottrip.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.network.service.FcmService
import com.app.myfoottrip.util.NetworkResult

class FcmRepository {
    private val headerFcmService = Application.headerRetrofit.create(FcmService::class.java)

    private val _addTokenResponseLiveData = MutableLiveData<NetworkResult<String>>()
    val addTokenResponseLiveData: LiveData<NetworkResult<String>>
        get() = _addTokenResponseLiveData

    //토큰 저장
    suspend fun addFcmToken(token: String){
        var response = headerFcmService.addFcmToken(token)

        if (response.isSuccessful && response.body() != null) {
            _addTokenResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _addTokenResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _addTokenResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }
}