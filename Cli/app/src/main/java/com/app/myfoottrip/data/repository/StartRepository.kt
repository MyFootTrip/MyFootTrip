package com.app.myfoottrip.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.network.api.TokenApi
import com.app.myfoottrip.util.NetworkResult
import retrofit2.Response

class StartRepository {
    private val tokenApi = Application.retrofit.create(TokenApi::class.java)

    private val _refreshTokenResponseLiveData = MutableLiveData<NetworkResult<Response<Token>>>()
    val refreshTokenResponseLiveData: LiveData<NetworkResult<Response<Token>>>
        get() = _refreshTokenResponseLiveData

    // refreshToken 제출 통신

}
/*
와 이 키보드 윤활 하신건가요?
좀 어색하긴한데 괜찮네요
스페이스바도 조금 시끄럽긴한데
이거 왜 이제 못 산다고 하셨죠?
 */