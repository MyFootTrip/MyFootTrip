package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.repository.FcmRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.launch

class FcmViewModel : ViewModel() {
    private val fcmRepository = FcmRepository()

    val addFcmToken: LiveData<NetworkResult<String>>
        get() = fcmRepository.addTokenResponseLiveData

    // 토큰 저장
    fun addFcmToken(token: String) {
        viewModelScope.launch {
            fcmRepository.addFcmToken(token)
        }
    }
}