package com.app.myfoottrip.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.network.api.TokenApi
import com.app.myfoottrip.util.NetworkResult
import retrofit2.Response

class StartRepository {
    private val tokenApi = Application.retrofit.create(TokenApi::class.java)

    private val _refreshTokenResponseLiveData = MutableLiveData<NetworkResult<String>>()
    val refreshTokenResponseLiveData: LiveData<NetworkResult<String>>
        get() = _refreshTokenResponseLiveData

    // refreshToken 유효성 체크 통신
    suspend fun checkRefreshToken(refresh_token: String) {
        val response = tokenApi.refreshTokenAvailableCheck(refresh_token)

        _refreshTokenResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _refreshTokenResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _refreshTokenResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _refreshTokenResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of checkRefreshToken
} // End of StartRepository class