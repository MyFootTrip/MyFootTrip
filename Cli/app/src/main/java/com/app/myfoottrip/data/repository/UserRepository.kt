package com.app.myfoottrip.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Email
import com.app.myfoottrip.data.dto.Join
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.network.api.TokenApi
import com.app.myfoottrip.network.api.UserApi
import com.app.myfoottrip.util.NetworkResult
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody


private const val TAG = "싸피"

class UserRepository {
    private val userApi = Application.retrofit.create(UserApi::class.java)
    private val tokenApi = Application.retrofit.create(TokenApi::class.java)

    // 헤더를 포함한 Retrofit
    private val headerTokenApi = Application.headerRetrofit.create(TokenApi::class.java)
    private val headerUserApi = Application.headerRetrofit.create(UserApi::class.java)

    private val _userResponseLiveData = MutableLiveData<NetworkResult<Boolean>>()
    val userResponseLiveData: LiveData<NetworkResult<Boolean>>
        get() = _userResponseLiveData

    private val _emailValidateResponseLiveData = MutableLiveData<NetworkResult<Boolean>>()
    val emailValidateResponseLiveData: LiveData<NetworkResult<Boolean>>
        get() = _emailValidateResponseLiveData

    private val _userJoinResponseLiveData = MutableLiveData<NetworkResult<Token>>()
    val userJoinResponseLiveData: LiveData<NetworkResult<Token>>
        get() = _userJoinResponseLiveData


    private val _userLoginReponseLiveData = MutableLiveData<NetworkResult<Token>>()
    val userLoginReponseLiveData: LiveData<NetworkResult<Token>>
        get() = _userLoginReponseLiveData

    private val _getUserDataResponseLiveData = MutableLiveData<NetworkResult<Join>>()
    val getUserDataResponseLiveData: LiveData<NetworkResult<Join>>
        get() = _getUserDataResponseLiveData


    // 이메일 중복 체크
    suspend fun checkUsedEmailId(emailId: Email) {

        try {
            val response = userApi.isUsedEmailId(emailId)

            // 처음은 Loading 상태로 지정
            _userResponseLiveData.postValue(NetworkResult.Loading())

            if (response.isSuccessful && response.body() != null) {
                _userResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
            } else if (response.errorBody() != null) {
                _userResponseLiveData.postValue(
                    NetworkResult.Error(
                        response.errorBody()!!.string()
                    )
                )
            } else {
                _userResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
            }
        } catch (e: java.lang.Exception) {

        }

    } // End of checkEmailId

    // 이메일 인증번호 체크
    suspend fun checkEmailValidateText(emailValidateData: Email) {
        val response = userApi.emailValidateCheck(emailValidateData)
        _emailValidateResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
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

    // 사용자 회원가입 체크
    suspend fun userJoin(
        userProfileImgFile: MultipartBody.Part?,
        userJoinData: HashMap<String, RequestBody>
    ) {
        val response = userApi.userJoin(
            userProfileImgFile,
            userJoinData
        )

        _userJoinResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _userJoinResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _userJoinResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _userJoinResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of joinUser

    // 유저 로그인
    suspend fun userLogin(
        email: String,
        password: String
    ) {
        val param = JsonObject().apply {
            addProperty("email", email)
            addProperty("password", password)
        }
        val response = userApi.userLogin(param)
        _userLoginReponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _userLoginReponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _userLoginReponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _userLoginReponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of userLogin


    // 토큰을 사용해서 유저 정보가져오기
    suspend fun getUserDataByAccessToken() {
        val response = headerTokenApi.getUserDataByAccessToken()

        _getUserDataResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _getUserDataResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _getUserDataResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _getUserDataResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }

    } // End of getUserData
} // End of UserRepository
