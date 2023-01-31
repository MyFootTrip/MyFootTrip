package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dao.VisitPlaceRepository
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.viewmodel.TravelViewModel
import com.app.myfoottrip.databinding.FragmentEditSaveTravelBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import com.naver.maps.map.MapView
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

private const val TAG = "EditSaveTravelFragment_싸피"

class EditSaveTravelFragment : BaseFragment<FragmentEditSaveTravelBinding>(
    FragmentEditSaveTravelBinding::bind, R.layout.fragment_edit_save_travel
) {

    lateinit var mapView: MapView
    private lateinit var mContext: Context
    lateinit var visitPlaceRepository: VisitPlaceRepository
    private val travelViewModel by activityViewModels<TravelViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitPlaceRepository = VisitPlaceRepository.get()
    } // End of onCreate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapFragment

        createTravelResponseObserve()

        // 버튼 클릭시 일어나는 모든 이벤트를 관리하는 메소드
        buttonEvents()

        // SQL Lite의 데이터를 불러와서 UI를 뿌리기
        setUI()
    } // End of onViewCreated

    // SQL Lite의 데이터를 불러와서 UI를 뿌리기
    private fun setUI() {


    } // End of setUI


    private fun buttonEvents() {
        // 저장 버튼 눌렀을 때 이벤트
        binding.travelEditSaveButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                createTravel()
            }
        }
    } // End of buttonEvents

    private suspend fun createTravel() {
        CoroutineScope(Dispatchers.IO).launch {
            var tempList: List<Place> = emptyList()

            val job1 = CoroutineScope(Dispatchers.IO).launch {
                tempList = getSqlLiteAllData()
            }

            job1.join()

            Log.d(TAG, "travelViewModel.selectLocationList: ${travelViewModel.locationList} ")

            val createTravelData = Travel(
                null,
                travelViewModel.locationList,
                tempList[0].saveDate, // 처음 시작 저장 시간
                Date(System.currentTimeMillis()), // 마지막 저장 시간
                tempList
            )

            // 변환된 Travel데이터를 서버에 저장
            coroutineScope {
                travelViewModel.createTravel(createTravelData)
            }
        }
    } // End of createTravel

    private suspend fun getSqlLiteAllData(): List<Place> {
        val placeList: MutableList<Place> = ArrayList()

        val job = CoroutineScope(Dispatchers.IO).async {
            val allVisitPlaceList = visitPlaceRepository.getVisitPlace()

            // visitPlace를 변경해서 Place로 변경
            val size = allVisitPlaceList.size

            // SQLLite 전체 데이터 가져오기
            for (i in 0 until size) {
                val temp = allVisitPlaceList[i]

                placeList.add(
                    Place(
                        null,
                        "",
                        temp.date?.let { Date(it) },
                        "", // 일단 처음에는 메모 빈 값
                        ArrayList(), // 일단 빈 이미지를 넣어야됨
                        temp.lat, // 좌표
                        temp.lng, // 좌표
                        getAddressByCoordinates(temp.lat, temp.lng)!!.getAddressLine(0)
                    )
                )
            }
        }

        job.await()
        job.join()

        return placeList
    } // End of getSqlLiteAllData


    private fun createTravelResponseObserve() {
        travelViewModel.createTravelResponseLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    if (it.data == 201) {
                        // 성공하면, SQLLite table 모두 비우기
                        CoroutineScope(Dispatchers.IO).launch {
                            visitPlaceRepository.deleteAllVisitPlace()

                            // DB를 비우고 빠져나가기
                            coroutineScope {
                                withContext(Dispatchers.Main) {
                                    findNavController().popBackStack()
                                }
                            }
                        }
                    }
                }

                is NetworkResult.Error -> {
                    binding.root.showSnackBarMessage("유저 여행 데이터 저장 오류 발생")
                    Log.d(TAG, "createTravelResponseLiveData Error: ${it.data}")
                    Log.d(TAG, "createTravelResponseLiveData Error: ${it.message}")
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "createTravelResponseLiveData Loading")
                }
            }
        }
    } // End of createTravelResponseObserve


    private fun getAddressByCoordinates(latitude: Double, longitude: Double): Address? {
        val geocoder = Geocoder(mContext, Locale.KOREA)

        val addresses: List<Address>?

        addresses = try {
            geocoder.getFromLocation(latitude, longitude, 7)
        } catch (ioException: IOException) {
            binding.root.showSnackBarMessage("지오코더 서비스 사용불가")
            ioException.printStackTrace()
            return null
        } catch (illegalArgumentException: java.lang.IllegalArgumentException) {
            illegalArgumentException.printStackTrace()
            binding.root.showSnackBarMessage("잘못된 위도 경도 입니다.")
            return null
        }

        if (addresses == null || addresses.isEmpty()) {
            binding.root.showSnackBarMessage("주소가 발견되지 않았습니다.")
            return null
        }

        val address: Address = addresses[0]
        return address
    } // End of getAddressByCoordinates

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
} // End of EditSaveTravelFragment class
