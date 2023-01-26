package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Email
import com.app.myfoottrip.data.dto.Join
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.data.dto.User
import com.app.myfoottrip.data.repository.UserRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    val userLoginResponseLiveData: LiveData<NetworkResult<Token>>
        get() = userRepository.userLoginReponseLiveData

    val getUserDataResponseLiveData: LiveData<NetworkResult<Join>>
        get() = userRepository.getUserDataResponseLiveData


    // 사용자 로그인
    suspend fun userLogin(emailId: String, password: String) {
        viewModelScope.launch {
            userRepository.userLogin(emailId, password)
        }
    } // End of userLogin

    suspend fun getUserDataByAccessToken() {
        viewModelScope.launch {
            userRepository.getUserDataByAccessToken()
        }
    } // End of getUserDataByAccessToken
} // End of UserViewModel class
