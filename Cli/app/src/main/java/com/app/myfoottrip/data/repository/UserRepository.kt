package com.app.myfoottrip.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.*
import com.app.myfoottrip.network.api.UserApi
import com.app.myfoottrip.util.ErrorInterceptUtil
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import org.json.JSONObject
import retrofit2.Response


private const val TAG = "싸피"

class UserRepository {
    private val userApi = Application.retrofit.create(UserApi::class.java)

    private val _userResponseLiveData = MutableLiveData<NetworkResult<Boolean>>()
    val userResponseLiveData: LiveData<NetworkResult<Boolean>>
        get() = _userResponseLiveData

    private val _emailValidateResponseLiveData = MutableLiveData<NetworkResult<Boolean>>()
    val emailValidateResponseLiveData: LiveData<NetworkResult<Boolean>>
        get() = _emailValidateResponseLiveData

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
        Log.d(TAG, "인증하는 이메일 정보:  ${emailValidateData}")


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

    suspend fun joinUser(joinUserData: Join): JoinTest {
        var result = JoinTest()
        Log.d(TAG, "요청 보낸 회원정보 : ${joinUserData}")
        Log.d(TAG, "result: $result")

        withContext(Dispatchers.IO) {
            try {
                var response = userApi.userJoin(joinUserData)
                if (response.isSuccessful) {
                    // 실패할 경우 아예 response.body 값 자체가 null값이 넘어옴
                    result = response.body() as JoinTest
                } else {
                    Log.d(TAG, "response 실패 값 : ${response.errorBody()!!.string()}")
                    val jsonObjectError = JSONObject(response.errorBody().toString())


                    Log.d(
                        TAG,
                        "joinUser ${jsonObjectError.getJSONObject("error").getString("message")}  "
                    )

                }
            } catch (e: java.lang.Exception) {
                Log.d(TAG, "joinUser: $")
            }
        }

        return result
    } // End of joinUser

} // End of UserRepository
