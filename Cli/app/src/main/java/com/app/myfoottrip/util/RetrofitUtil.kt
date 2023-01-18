package com.app.myfoottrip.util

import com.app.myfoottrip.Application
import com.app.myfoottrip.network.service.UserService

class RetrofitUtil {
    companion object{
        val userService = Application.retrofit.create(UserService::class.java)
    }
}