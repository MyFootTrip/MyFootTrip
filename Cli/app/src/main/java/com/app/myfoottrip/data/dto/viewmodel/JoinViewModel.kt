package com.app.myfoottrip.data.model.viewmodel


import android.graphics.Paint
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Email
import com.app.myfoottrip.data.dto.Image
import com.app.myfoottrip.data.dto.JoinTest
import com.app.myfoottrip.data.dto.User
import com.app.myfoottrip.data.repository.UserRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

private const val TAG = "싸피"

class JoinViewModel : ViewModel() {
    private val userRepository = UserRepository()

    // 휴대폰 인증 상태 체크 LiveData
    private val _phoneNumberValidation = MutableLiveData<Boolean>()
    val phoneNumberValidation: LiveData<Boolean>
        get() = _phoneNumberValidation

    // 나이 상태 체크
    private val _ageState =
        MutableLiveData<List<Boolean>>(mutableListOf(false, false, false, false, false, false))
    val ageState: LiveData<List<Boolean>>
        get() = _ageState

    // 동의한 약관 개수 파악
    private val _isAllCheckedCount = MutableLiveData(0)
    val isAllCheckedCount: LiveData<Int>
        get() = _isAllCheckedCount

    // 이메일 체크 상태
    private val _emailCheckSuccess = MutableLiveData<Boolean>(false)
    val emailCheckSuccess: LiveData<Boolean>
        get() = _emailCheckSuccess

    // 회원가입 하면서 생성되는 유저 정보를 viewModel에 저장
    private val _joinUserData =
        MutableLiveData<JoinTest>()
    val joinUserData: LiveData<JoinTest>
        get() = _joinUserData

    private val _joinResponseStatus = MutableLiveData(false)
    val joinResponseStatus: LiveData<Boolean>
        get() = _joinResponseStatus

    private val _joinSuccessUserData = MutableLiveData<User>()
    val joinSuccessUserData: LiveData<User>
        get() = _joinSuccessUserData


    // 이메일 중복 체크 변경 테스트 LiveData
    val userResponseLiveData: LiveData<NetworkResult<Boolean>>
        get() = userRepository.userResponseLiveData

    // 이메일 중복체크 & 인증번호를 관리하는 LiveData
    val emailValidateResponse: LiveData<NetworkResult<Boolean>>
        get() = userRepository.emailValidateResponseLiveData

    // 비밀번호 값 비교용
    private val _originPassword = MutableLiveData<String>()
    val originPassword: LiveData<String>
        get() = _originPassword

    // 확인 비밀번호
    private val _confirmPassword = MutableLiveData<String>()
    val confirmPassword: LiveData<String>
        get() = _confirmPassword

    // 회원가입 responseLiveData
    private val _joinResponseLiveData: LiveData<NetworkResult<User>>
        get() = userRepository.userJoinResponseLiveData

    // 이메일 중복 체크
    fun emailUsedCheck(emailId: Email) {
        viewModelScope.launch {
            userRepository.checkUsedEmailId(emailId)
        }
    } // End of emailUsedCheck

    // 이메일 인증 문자 체크
    fun emailValidateCheck(emailValidateData: Email) {
        viewModelScope.launch {
            userRepository.checkEmailValidateText(emailValidateData)
        }
    } // End of emailValidateCheck

    // 동의 체크한 목록 개수값 변경
    fun setCheckedCountValue(count: Int) {
        _isAllCheckedCount.value = 0
        _isAllCheckedCount.value = count
    } // End of setCheckedCountValue

    // 날짜 상태 배열 값 변경
    fun changeAgeState(index: Int) {
        val tempList = mutableListOf(false, false, false, false, false, false)
        tempList[index] = true

        _ageState.value = tempList
    } // End of changeAgeState


    private val _userImageData = MutableLiveData<MultipartBody.Part>()
    val userImageData: LiveData<MultipartBody.Part>
        get() = _userImageData


    // 사용자 회원가입 시작 (retrofit 통신)
    fun userJoin() {
        viewModelScope.launch {
            //userRepository.joinUser()
        }
    } // End of userJoin

    fun setPwLiveData(pwOrigin: String, pwConfirm: String) {
        _originPassword.value = pwOrigin
        _confirmPassword.value = pwConfirm
    } // End of setOriginPw


} // End of JoinViewModel
