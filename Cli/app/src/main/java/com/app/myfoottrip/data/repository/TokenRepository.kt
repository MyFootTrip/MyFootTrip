package com.app.myfoottrip.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.myfoottrip.Application
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.data.dto.User
import com.app.myfoottrip.network.api.TokenApi
import com.app.myfoottrip.util.NetworkResult
import com.google.gson.JsonObject

private const val TAG = "TokenRepository_싸피"

class TokenRepository {
    private val tokenApi = Application.retrofit.create(TokenApi::class.java)

    // 헤더를 포함한 Retrofit
    private val headerTokenApi = Application.headerRetrofit.create(TokenApi::class.java)

    // 액세스 토큰이 만료되었을 때 리프레시 토큰을 통해서 액세스토큰을 재발급 요청
    private val _getAccessTokenByRefreshTokenResponseLiveData =
        MutableLiveData<NetworkResult<Token>>()
    val getAccessTokenByRefreshTokenResponseLiveData: LiveData<NetworkResult<Token>>
        get() = _getAccessTokenByRefreshTokenResponseLiveData


    // 액세스 토큰을 통해서 유저 데이터를 요청
    private val _getUserDataByAccessTokenResponseLiveData = MutableLiveData<NetworkResult<User>>()
    val getUserDataByAccessTokenResponseLiveData: LiveData<NetworkResult<User>>
        get() = _getUserDataByAccessTokenResponseLiveData

    private val _getUserDataResponseLiveData = MutableLiveData<NetworkResult<User>>()
    val getUserDataResponseLiveData: LiveData<NetworkResult<User>>
        get() = _getUserDataResponseLiveData

    private val _deleteRefreshTokenResponseLiveData = MutableLiveData<NetworkResult<String>>()
    val deleteRefreshTokenResponseLiveData: LiveData<NetworkResult<String>>
        get() = _deleteRefreshTokenResponseLiveData

    suspend fun getAccessTokenByRefreshToken(refreshToken: Token) {
        val response = tokenApi.refreshTokenAvailableCheck(refreshToken)

        _getAccessTokenByRefreshTokenResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _getAccessTokenByRefreshTokenResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _getAccessTokenByRefreshTokenResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _getAccessTokenByRefreshTokenResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of getAccessTokenByRefreshToken

    // AccessToken 헤더를 사용해서 유저 정보가져오기
    suspend fun getUserDataByAccessToken() {
        val response = headerTokenApi.getUserDataByAccessToken()

        _getUserDataByAccessTokenResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _getUserDataByAccessTokenResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _getUserDataByAccessTokenResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _getUserDataByAccessTokenResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of getUserDataByAccessToken

    suspend fun getUserData() {
        val response = headerTokenApi.getUserDataByAccessToken()

        _getUserDataResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _getUserDataResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _getUserDataResponseLiveData.postValue(
                NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _getUserDataResponseLiveData.postValue(
                NetworkResult.Error(response.headers().toString()))
        }

    } // End of getUserDataByAccessToken

    // refreshToken 삭제
    suspend fun deleteRefreshToken(token: String){
        var response = headerTokenApi.deleteRefreshToken(token)

        if (response.isSuccessful && response.body() != null) {
            _deleteRefreshTokenResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _deleteRefreshTokenResponseLiveData.postValue(NetworkResult.Error(response.errorBody()!!.string()))
        } else {
            _deleteRefreshTokenResponseLiveData.postValue(NetworkResult.Error(response.headers().toString()))
        }
    }

    // =================================== Social Login ===================================
    private val _postSocialLoginAccessTokenResponseLiveData = MutableLiveData<NetworkResult<Token>>()
    val postSocialLoginAccessTokenResponseLiveData: LiveData<NetworkResult<Token>>
        get() = _postSocialLoginAccessTokenResponseLiveData


    // =================================== Naver Login ===================================
    private val _postNaverAccessTokenResponseLiveData = MutableLiveData<NetworkResult<Token>>()
    val postNaverAccessTokenResponseLiveData: LiveData<NetworkResult<Token>>
        get() = _postNaverAccessTokenResponseLiveData

    suspend fun postNaverLoginAccessToken(token: String) {
        val param = JsonObject().apply {
            addProperty("token", token)
        }
        val response = tokenApi.postNaverLoginAccessToken(param)

        _postSocialLoginAccessTokenResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _postSocialLoginAccessTokenResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _postSocialLoginAccessTokenResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _postSocialLoginAccessTokenResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of postNaverAccessToken

    // =================================== Kakao Login ===================================
    private val _postKakaoAccessTokenResponseLiveData = MutableLiveData<NetworkResult<Token>>()
    val postKakaoAccessTokenResponseLiveData: LiveData<NetworkResult<Token>>
        get() = _postKakaoAccessTokenResponseLiveData

    suspend fun postKakaoLoginAccessToken(token: String) {
        val param = JsonObject().apply {
            addProperty("token", token)
        }

        val response = tokenApi.postKakaoLoginAccessToken(param)

        _postSocialLoginAccessTokenResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _postSocialLoginAccessTokenResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _postSocialLoginAccessTokenResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _postSocialLoginAccessTokenResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of postKakaoAccessToken

    // =================================== Google Login ===================================
    private val _postGoogleAccessTokenResponseLiveData = MutableLiveData<NetworkResult<Token>>()
    val postGoogleAccessTokenResponseLiveData: LiveData<NetworkResult<Token>>
        get() = _postGoogleAccessTokenResponseLiveData

    suspend fun postGoogleLoginAccessToken(token: String) {
        val param = JsonObject().apply {
            addProperty("token", token)
        }

        val response = tokenApi.postGoogleLoignAccessToken(param)

        _postSocialLoginAccessTokenResponseLiveData.postValue(NetworkResult.Loading())

        if (response.isSuccessful && response.body() != null) {
            _postSocialLoginAccessTokenResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        } else if (response.errorBody() != null) {
            _postSocialLoginAccessTokenResponseLiveData.postValue(
                NetworkResult.Error(
                    response.errorBody()!!.string()
                )
            )
        } else {
            _postSocialLoginAccessTokenResponseLiveData.postValue(
                NetworkResult.Error(
                    response.headers().toString()
                )
            )
        }
    } // End of postGoogleLoignAccessToken
} // End of TokenRepository class


