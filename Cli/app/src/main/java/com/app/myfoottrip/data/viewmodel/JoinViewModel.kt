package com.app.myfoottrip.data.viewmodel


import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Email
import com.app.myfoottrip.data.dto.Join
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.data.repository.UserRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

private const val TAG = "싸피"

class JoinViewModel : ViewModel() {
    private val userRepository = UserRepository()

    // 나이 상태 체크
    private val _ageState =
        MutableLiveData<List<Boolean>>(mutableListOf(false, false, false, false, false, false))
    val ageState: LiveData<List<Boolean>>
        get() = _ageState

    // 동의한 약관 개수 파악
    private val _isAllCheckedCount = MutableLiveData(0)
    val isAllCheckedCount: LiveData<Int>
        get() = _isAllCheckedCount

    // 회원가입 하면서 생성되는 전체 유저 정보 LiveData
    private val _wholeJoinUserData = Join("", "")
    val wholeJoinUserData: Join
        get() = _wholeJoinUserData


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

    // 확인 비밀번호1
    private val _confirmPassword = MutableLiveData<String>()
    val confirmPassword: LiveData<String>
        get() = _confirmPassword

    // 회원가입 Response 값 responseLiveData
    val joinResponseLiveData: LiveData<NetworkResult<Token>>
        get() = userRepository.userJoinResponseLiveData

    // 유저 이미지를 서버에 보내기 위해서 값을 가지고 있는 LiveData
    private val _userProfileImageMultipartBody = MutableLiveData<MultipartBody.Part>()
    val userProfileImageMultipartBody: LiveData<MultipartBody.Part>
        get() = _userProfileImageMultipartBody

    // 유저 이미지를 관리하는 LiveData
    private val _userProfileImage = MutableLiveData<Uri>()
    val userProfileImage: LiveData<Uri>
        get() = _userProfileImage

    // 이메일 중복 체크 통신
    fun emailUsedCheck(emailId: Email) {
        viewModelScope.launch {
            userRepository.checkUsedEmailId(emailId)
        }
    } // End of emailUsedCheck

    // 사용자 회원가입 통신
    fun userJoin() {
        var requestHashMap: HashMap<String, RequestBody> = HashMap()

        requestHashMap["email"] =
            _wholeJoinUserData.email.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        requestHashMap["password"] =
            _wholeJoinUserData.password.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        requestHashMap["username"] =
            _wholeJoinUserData.username.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        requestHashMap["nickname"] =
            _wholeJoinUserData.nickname.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        requestHashMap["age"] =
            _wholeJoinUserData.age.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        viewModelScope.launch {
            userRepository.userJoin(
                _userProfileImageMultipartBody.value, requestHashMap
            )
        }
    } // End of userJoin

    // 이메일 인증 문자 체크1
    fun emailValidateCheck(emailValidateData: Email) {
        viewModelScope.launch {
            userRepository.checkEmailValidateText(emailValidateData)
        }
    } // End of emailValidateCheck

    // 동의 체크한 목록 개수값 변경
    fun setCheckedCountValue(count: Int) {
        _isAllCheckedCount.value = 0 // 0으로 초기화하고 난 후 값을 변경
        _isAllCheckedCount.value = count
    } // End of setCheckedCountValue

    // 날짜 상태 배열 값 변경
    fun changeAgeState(index: Int) {
        val tempList = mutableListOf(false, false, false, false, false, false)
        tempList[index] = true
        _ageState.value = tempList
    } // End of changeAgeState

    fun setUserImageUri(imageUri: Uri) {
        _userProfileImage.value = imageUri
    } // End of setUserImageUri

    fun setUserImageUriToMultipart(imageUri: MultipartBody.Part) {
        _userProfileImageMultipartBody.value = imageUri
    } // End of setUserImageUri

    fun setPwLiveData(pwOrigin: String, pwConfirm: String) {
        _originPassword.value = pwOrigin
        _confirmPassword.value = pwConfirm
    } // End of setOriginPw
} // End of JoinViewModel
