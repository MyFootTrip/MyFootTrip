package com.app.myfoottrip.data.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditSaveViewModel : ViewModel() {

    // 유저가 선택한 이미지를 저장함
    private val _selectUserImageList = MutableLiveData<Uri>()
    val selectUserImageList: LiveData<Uri>
        get() = _selectUserImageList

    fun setSelectUserImageList(imageUri : Uri) {
        _selectUserImageList.value = imageUri
    } // End of setSelectUserImageList



} // End of EditSaveViewModel class
