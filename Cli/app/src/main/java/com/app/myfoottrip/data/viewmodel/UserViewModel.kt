package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.User
import com.app.myfoottrip.data.repository.UserRepository
import com.app.myfoottrip.util.NetworkResult
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _loginSuccessUserData = MutableLiveData<User>()
    val loginSuccessUserData: LiveData<User>
        get() = _loginSuccessUserData

    val userLoginResponseLiveData: LiveData<NetworkResult<JsonPrimitive>>
        get() = userRepository.userLoginReponseLiveData


    suspend fun userLogin(emailId: String, password: String) {
        viewModelScope.launch {
            userRepository.userLogin(emailId, password)
        }
    }


} // End of UserViewModel class