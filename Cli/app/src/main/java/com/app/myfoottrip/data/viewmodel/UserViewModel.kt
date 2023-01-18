package com.app.myfoottrip.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.myfoottrip.data.dto.User

class UserViewModel : ViewModel() {

    private val _loginSuccessUserData = MutableLiveData<User>()
    val loginSuccessUserData: LiveData<User>
        get() = _loginSuccessUserData


} // End of UserViewModel class