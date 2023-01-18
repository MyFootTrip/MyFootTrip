package com.app.myfoottrip.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.*
import com.app.myfoottrip.network.api.UserApi
import com.app.myfoottrip.util.NetworkResult
import okhttp3.MultipartBody


private const val TAG = "싸피"

class UserRepository {
    private val userApi = Application.retrofit.create(UserApi::class.java)

    private val _userResponseLiveData = MutableLiveData<NetworkResult<Boolean>>()
    val userResponseLiveData: LiveData<NetworkResult<Boolean>>
        get() = _userResponseLiveData

    private val _emailValidateResponseLiveData = MutableLiveData<NetworkResult<Boolean>>()
    val emailValidateResponseLiveData: LiveData<NetworkResult<Boolean>>
        get() = _emailValidateResponseLiveData

    private val _userJoinResponseLiveData = MutableLiveData<NetworkResult<User>>()
    val userJoinResponseLiveData: LiveData<NetworkResult<User>>
        get() = _userJoinResponseLiveData

    // 이메일 중복 체크
    suspend fun checkUsedEmailId(emailId: Email) {
        val response = userApi.isUsedEmailId(emailId)

        // 처음은 Loading 상태로 지정
        _userResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _userResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _userResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _userResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }

    } // End of checkEmailId

    suspend fun checkEmailValidateText(emailValidateData: Email) {
        val response = userApi.emailValidateCheck(emailValidateData)
        _emailValidateResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            Log.d(TAG, "checkEmailValidateText: 여기로 들어가나요?")
            _emailValidateResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _emailValidateResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _emailValidateResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of checkEmailValidateText

    suspend fun joinUser(file : MultipartBody.Part) {


        val response = userApi.imageTest(file)
        Log.d(TAG, "joinUser: $response")

//        val response = userApi.userJoin(joinUserData)
//        _userJoinResponseLiveData.postValue(NetworkResult.Loading())
//
//        if (response.isSuccessful && response.body() != null) {
//            _userJoinResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
//        } else if (response.errorBody() != null) {
//            _userJoinResponseLiveData.postValue(
//                NetworkResult.Error(
//                    response.errorBody()!!.string()
//                )
//            )
//        } else {
//            _userJoinResponseLiveData.postValue(
//                NetworkResult.Error(
//                    response.headers().toString()
//                )
//            )
//        }
    } // End of joinUser
} // End of UserRepository
