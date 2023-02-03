package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.data.dto.User
import com.app.myfoottrip.data.repository.TokenRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TokenViewModel : ViewModel() {
    private val tokenRepository = TokenRepository()

    // AccessToken이 만료되었을 때 RefreshToken을 통해서 AccessToken을 다시 요청함
    val getAccessTokenByRefreshTokenResponseLiveData: LiveData<NetworkResult<Token>>
        get() = tokenRepository.getAccessTokenByRefreshTokenResponseLiveData

    val getUserDataByAccessTokenResponseLiveData: LiveData<NetworkResult<User>>
        get() = tokenRepository.getUserDataByAccessTokenResponseLiveData

    val getUserResponseLiveData: LiveData<NetworkResult<User>>
        get() = tokenRepository.getUserDataResponseLiveData

    val deleteRefreshTokenResponseLiveData: LiveData<NetworkResult<String>>
        get() = tokenRepository.deleteRefreshTokenResponseLiveData

    suspend fun getAccessTokenByRefreshToken(refreshToken: Token) {
        tokenRepository.getAccessTokenByRefreshToken(refreshToken)
    } // End of getAccessTokenByRefreshToken

    suspend fun getUserDataByAccessToken() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tokenRepository.getUserDataByAccessToken()
            }
        }
    } // End of getUserDataByAccessToken

    fun getUserData() {
        viewModelScope.launch {
            tokenRepository.getUserData()
        }
    } // End of getUserDataByAccessToken

    fun deleteRefreshToken(token: String){
        viewModelScope.launch {
            tokenRepository.deleteRefreshToken(token)
        }
    }
} // End of TokenViewModel class
