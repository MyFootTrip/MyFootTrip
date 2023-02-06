package com.app.myfoottrip.data.viewmodel

import android.provider.ContactsContract.CommonDataKinds.Nickname
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Join
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.data.dto.User
import com.app.myfoottrip.data.repository.UserRepository
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

private const val TAG = "UserViewModel_싸피"

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    // 첫 번째 통신에 따른 전체 유저 데이터를 가져와서 ViewModel에 저장함
    private val _wholeMyData = MutableLiveData<User>()
    val wholeMyData: LiveData<User>
        get() = _wholeMyData

    // 로그인 결과값을 받아오는 LiveData
    val userLoginResponseLiveData: LiveData<NetworkResult<Token>>
        get() = userRepository.userLoginReponseLiveData

    val updateUserResponseLiveData: LiveData<NetworkResult<Join>>
        get() = userRepository.userUpdateReponseLiveData

    // 회원가입 하면서 생성되는 전체 유저 정보 LiveData
    private val _wholeUpdateUserData = Join("", "")
    val wholeUpdateUserData: Join
        get() = _wholeUpdateUserData

    // 유저 이미지를 서버에 보내기 위해서 값을 가지고 있는 LiveData
    private val _userProfileImageMultipartBody = MutableLiveData<MultipartBody.Part>()
    val userProfileImageMultipartBody: LiveData<MultipartBody.Part>
        get() = _userProfileImageMultipartBody

    // 사용자 로그인
    suspend fun userLogin(emailId: String, password: String) {
        viewModelScope.launch {
            userRepository.userLogin(emailId, password)
        }
    } // End of userLogin

    // =============================== 사용자 전체 데이터 ===============================
    fun setWholeMyData(myData: User) {
        _wholeMyData.value = myData
    } // End of setWholeMyData


    //유저 정보 수정
    fun userUpdate() {
        var requestHashMap: HashMap<String, RequestBody> = HashMap()

        requestHashMap["email"] =
            _wholeUpdateUserData.email.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        requestHashMap["password"] =
            _wholeUpdateUserData.password.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        requestHashMap["username"] =
            _wholeUpdateUserData.username.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        requestHashMap["nickname"] =
            _wholeUpdateUserData.nickname.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        requestHashMap["age"] =
            _wholeUpdateUserData.age.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        viewModelScope.launch {
            userRepository.updateUser(
                _userProfileImageMultipartBody.value, requestHashMap
            )
        }
    }

} // End of UserViewModel class
