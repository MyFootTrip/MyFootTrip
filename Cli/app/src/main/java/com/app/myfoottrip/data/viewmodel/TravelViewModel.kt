package com.app.myfoottrip.data.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.myfoottrip.data.dto.Coordinates
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.dto.TravelPush
import com.app.myfoottrip.data.repository.TravelRepository
import com.app.myfoottrip.util.NetworkResult
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "TravelViewModel_싸피"

class TravelViewModel : ViewModel() {

//    init {
//        viewModelScope.launch {
//            EventBus.subscribe<Coordinates>().collect {
//                value ->
//                Log.d(TAG, "eventBus: $value")
//                _recentCoor.value = (value)
//            }
//        }
//    }

    private val travelRepository = TravelRepository()
    var selectedtravel: Travel? = null

    // 가장 최근에 찍힌 좌표값
    private val _recentCoor = MutableLiveData<Coordinates>()
    val recentCoor: LiveData<Coordinates>
        get() = _recentCoor

    var travelList: ArrayList<Travel> = arrayListOf()

    fun setRecentCoor(newCoordinates: Coordinates) {
        _recentCoor.postValue(newCoordinates)
    } // End of setRecentCoor

    //여정 조회 값
    private val _travelData = MutableLiveData<Travel>()
    val travelData: LiveData<Travel>
        get() = _travelData

    fun setCreateTravelResponseLiveData(newData : NetworkResult<Int>?) {
        travelRepository.setCreateTravelResponseLiveData(newData)
    } // End of setCreateTravelResponseLiveData


    // ======================== 여행정보를 새로만드는지, 기존의 데이터를 불러오는 뷰인지 구분 ========================
    // 여행정보를 새로만드는지, 기존의 데이터를 불러오는 뷰인지 구분하기 위한 LiveData
    private val _userTravelDataNewOrUpdateCheck = MutableLiveData<Boolean?>(null)
    val userTravelDataNewOrUpdateCheck: MutableLiveData<Boolean?>
        get() = _userTravelDataNewOrUpdateCheck

    fun setUserTravelDataNewOrUpdateCheck(flag: Boolean?) {
        userTravelDataNewOrUpdateCheck.value = flag
    } // End of setUserTravelDataNewOrUpdateCheck

    // ======================== 유저 전체 여행 리스트 조회 ========================
    val travelUserData: LiveData<NetworkResult<ArrayList<Travel>>>
        get() = travelRepository.travelListResponseLiveData

    suspend fun getUserTravel(userId: Int) {
        viewModelScope.launch {
            travelRepository.getUserTravel(userId)

        }
    } // End of getUserTravel

    // ======================== 유저 여행 데이터 조회 ========================
    // 기존의 여행 데이터를 가져오는 response 값
    val getUserTravelDataResponseLiveData: LiveData<NetworkResult<Travel>>
        get() = travelRepository.getUserTravelDataResponseLiveData

    // 유저의 여행 데이터 조회
    fun getUserTravelData(travelId: Int) {
        viewModelScope.launch {
            travelRepository.getUserTravelData(travelId)
        }
    } // End of getUserTravelData

    // ======================== 유저 여행 데이터 추가 ========================
    // 유저 여행 데이터 추가 response값 LiveData
    val createTravelResponseLiveData: LiveData<NetworkResult<Int>?>
        get() = travelRepository.createTravelResponseLiveData

    fun createTravel(imageList: List<MultipartBody.Part>, travelData: TravelPush) {
        var requestHashMap: HashMap<String, RequestBody> = HashMap()

        val sdf = SimpleDateFormat("YYYY-MM-dd HH:mm:ss", Locale("ko", "KR"))
        val startDateString = travelData.startDate?.let { sdf.format(it) }
        val endDateString = travelData.endDate?.let { sdf.format(it) }

        val locationList = JSONArray(travelData.location)
        requestHashMap["location"] =
            locationList.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        requestHashMap["startDate"] =
            startDateString!!.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        requestHashMap["endDate"] =
            endDateString!!.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        // PlaceList를 JSON으로 변경
        val gsonPretty =
            GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
        val jsonPlaceList: String = gsonPretty.toJson(travelData.placeList!!)
        requestHashMap["placeList"] =
            jsonPlaceList.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        viewModelScope.launch {
            travelRepository.createTravel(imageList, requestHashMap)
        }
    } // End of createTravel

    // ======================== 유저 여행 데이터 수정 ========================
    // 여행 데이터 수정 response값 LiveData
    val userTravelDataUpdateResponseLiveData: LiveData<NetworkResult<Travel>>
        get() = travelRepository.userTravelDataUpdateResponseLiveData

    // 여행 데이터 수정
    suspend fun userTravelDataUpdate(
        travelId: Int, newImageList: List<MultipartBody.Part>, updateTravelData: TravelPush
    ) {
        var requestHashMap: HashMap<String, RequestBody> = HashMap()

        val sdf = SimpleDateFormat("YYYY-MM-dd HH:mm:ss", Locale("ko", "KR"))
        val startDateString = updateTravelData.startDate?.let { sdf.format(it) }
        val endDateString = updateTravelData.endDate?.let { sdf.format(it) }

        val locationList = JSONArray(updateTravelData.location)
        requestHashMap["location"] =
            locationList.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())
        requestHashMap["startDate"] =
            startDateString!!.toRequestBody("multipart/form-data".toMediaTypeOrNull())
        requestHashMap["endDate"] =
            endDateString!!.toRequestBody("multipart/form-data".toMediaTypeOrNull())

        // PlaceList를 JSON으로 변경
        val gsonPretty =
            GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
        val jsonPlaceList: String = gsonPretty.toJson(updateTravelData.placeList!!)
        requestHashMap["placeList"] =
            jsonPlaceList.toRequestBody("multipart/form-data".toMediaTypeOrNull())


        // 삭제할 데이터들
        val deletePlaceList = JSONArray(updateTravelData.deletePlaceList)
        requestHashMap["DeletePlaceList"] =
            deletePlaceList.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val deleteImageList = JSONArray(updateTravelData.deleteImageList)
        requestHashMap["DeleteImageList"] =
            deleteImageList.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull())

        Log.d(TAG, "삭제되는 이미지 리스트들: ${deleteImageList} ")

        viewModelScope.launch {
            travelRepository.userTravelDataUpdate(travelId, newImageList, requestHashMap)
        }
    } // End of userTravelDataUpdate

    // ======================== 유저 여행 데이터 삭제 ========================
    // 여행 데이터 삭제 response값 LiveData
    val userTravelDataDeleteResponseLiveData: LiveData<NetworkResult<Int>>
        get() = travelRepository.userTravelDataDeleteResponseLiveData

    // 여행 데이터 삭제
    suspend fun userTravelDataDelete(travelId: Int) {
        viewModelScope.launch {
            travelRepository.userTravelDataDelete(travelId)
        }
    } // End of userTravelDataDelete
} // End of TravelViewModel class
