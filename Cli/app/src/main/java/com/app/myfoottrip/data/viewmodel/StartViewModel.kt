package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.AccessToken
import com.app.myfoottrip.data.dto.RefreshToken
import com.app.myfoottrip.data.repository.StartRepository
import com.app.myfoottrip.util.NetworkResult
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.launch

class StartViewModel : ViewModel() {
    private val startRepository = StartRepository()

    // refreshToken 유효성 체크 response LiveData
    val refreshTokenValidCheckResponseLiveData: LiveData<NetworkResult<AccessToken>>
        get() = startRepository.refreshTokenResponseLiveData

    fun refreshTokenValidCheck(refreshToken: RefreshToken) {
        viewModelScope.launch {
            startRepository.checkRefreshToken(refreshToken)
        }
    } // End of refreshTokenValidCheck

} // End of StartViewModel class