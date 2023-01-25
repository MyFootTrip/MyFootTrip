package com.app.myfoottrip.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.network.api.TokenApi
import com.app.myfoottrip.util.NetworkResult

private const val TAG = "StartRepository_싸피"

class StartRepository {
    private val tokenApi = Application.retrofit.create(TokenApi::class.java)

    private val _refreshTokenResponseLiveData = MutableLiveData<NetworkResult<Token>>()
    val refreshTokenResponseLiveData: LiveData<NetworkResult<Token>>
        get() = _refreshTokenResponseLiveData

    // refreshToken 유효성 체크 통신
    suspend fun checkRefreshToken(refreshToken: Token) {

        try {
            val response = tokenApi.refreshTokenAvailableCheck(refreshToken)

            Log.d(TAG, "checkRefreshToken response: $response")
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
        } catch (e: Exception) {
            Log.e(TAG, "Exception: $e")
        }


    } // End of checkRefreshToken
} // End of StartRepository class
