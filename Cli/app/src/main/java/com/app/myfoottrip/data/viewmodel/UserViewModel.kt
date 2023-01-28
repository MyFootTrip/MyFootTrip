package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.data.dto.User
import com.app.myfoottrip.data.repository.UserRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.launch

private const val TAG = "UserViewModel_싸피"

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    // 첫 번째 통신에 따른 전체 유저 데이터를 가져와서 ViewModel에 저장함
    private val _wholeMyData = MutableLiveData<User>()
    val wholeMyData: LiveData<User>
        get() = _wholeMyData

    // 로그인 결과값을 받아오는 LiveData
    val userLoginResponseLiveData: LiveData<NetworkResult<Token>>
        get() = userRepository.userLoginReponseLiveData

    // 사용자 로그인
    suspend fun userLogin(emailId: String, password: String) {
        viewModelScope.launch {
            userRepository.userLogin(emailId, password)
        }
    } // End of userLogin

    fun setWholeMyData(myData: User) {
        _wholeMyData.value = myData
    } // End of setWholeMyData
} // End of UserViewModel class
