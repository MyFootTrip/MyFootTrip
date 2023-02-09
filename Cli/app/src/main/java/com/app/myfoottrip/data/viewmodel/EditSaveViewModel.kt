package com.app.myfoottrip.data.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.myfoottrip.data.dto.VisitPlace
import java.util.LinkedList

class EditSaveViewModel : ViewModel() {

    // 유저가 선택한 이미지를 저장함
    private val _selectUserImageList = MutableLiveData<Uri>()
    val selectUserImageList: LiveData<Uri>
        get() = _selectUserImageList

    fun setSelectUserImageList(imageUri: Uri) {
        _selectUserImageList.value = imageUri
    } // End of setSelectUserImageList


    // ======================================================= User VisitPlace LiveData =======================================================
    // 수정할 내용을 담고 있는 LiveData
    private val _userVisitPlaceData : MutableLiveData<VisitPlace> = MutableLiveData<VisitPlace>()
    val userVisitPlaceData: LiveData<VisitPlace>
        get() = _userVisitPlaceData

    fun setUserVisitPlaceData(data : VisitPlace?) {
        _userVisitPlaceData.value = data
    } // End of setUserVisitPlaceData

} // End of EditSaveViewModel class
