package com.app.myfoottrip.data.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.myfoottrip.data.dto.VisitPlace
import java.util.*

class EditSaveViewModel : ViewModel() {

    // 유저가 선택한 이미지를 저장함
    private val _selectUserImageList = MutableLiveData<Uri?>()
    val selectUserImageList: LiveData<Uri?>
        get() = _selectUserImageList

    fun setSelectUserImageLiveData(imageUri: Uri) {
        _selectUserImageList.value = imageUri
    } // End of setSelectUserImageLiveData

    fun clearSelectUserImageLiveData() {
        _selectUserImageList.value = null
    } // End of clearSelectUserImageLiveData

    // ======================================================= 삭제되는 정보들 =======================================================
    private val _deleteImageList: MutableList<String> = LinkedList<String>()
    val deleteImageList: List<String>
        get() = _deleteImageList

    fun addDeleteImageList(imageUrl: String) {
        _deleteImageList.add(imageUrl)
    } // End of addDeleteImageList

    fun clearDeleteImageList() {
        _deleteImageList.clear()
    } // End of clearDeleteImageList


    // ======================================================= User VisitPlace LiveData =======================================================
    // 수정할 내용을 담고 있는 LiveData
    private val _userVisitPlaceData: MutableLiveData<VisitPlace> = MutableLiveData<VisitPlace>()
    val userVisitPlaceData: LiveData<VisitPlace>
        get() = _userVisitPlaceData


    fun setUserVisitPlaceData(data: VisitPlace) {
        _userVisitPlaceData.value = data
    } // End of setUserVisitPlaceData

} // End of EditSaveViewModel class
