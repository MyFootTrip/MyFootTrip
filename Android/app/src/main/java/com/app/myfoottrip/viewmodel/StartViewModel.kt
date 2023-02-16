package com.app.myfoottrip.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.model.Token
import com.app.myfoottrip.repository.StartRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.launch

class StartViewModel : ViewModel() {
    private val startRepository = StartRepository()

    // refreshToken 유효성 체크 response LiveData
    val refreshTokenValidCheckResponseLiveData: LiveData<NetworkResult<Token>>
        get() = startRepository.refreshTokenResponseLiveData

    fun refreshTokenValidCheck(refreshToken: Token) {
        viewModelScope.launch {
            startRepository.checkRefreshToken(refreshToken)
        }
    } // End of refreshTokenValidCheck

} // End of StartViewModel class