package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dao.VisitPlaceRepository
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.dto.VisitPlace
import com.app.myfoottrip.data.viewmodel.TravelViewModel
import com.app.myfoottrip.databinding.FragmentEditSaveTravelBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.util.NetworkResult
import com.naver.maps.map.MapView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
    } // End of onViewCreated

    private fun buttonEvents() {
        // 저장 버튼 눌렀을 때 이벤트
        binding.travelEditSaveButton.setOnClickListener {
            // 수정된 데이터들을 모두 반영해서 저장한 뒤에 밖으로 나감

            Log.d(TAG, "travelEditSaveButton : 저장 버튼 클릭 ")

            // SQLLite 전체 데이터 가져오기
            var allVisitPlaceList: List<VisitPlace> = emptyList()

            CoroutineScope(Dispatchers.IO).launch {
                allVisitPlaceList = visitPlaceRepository.getVisitPlace()

                // visitPlace를 변경해서 Place로 변경
                val size = allVisitPlaceList.size
                val placeList: MutableList<Place> = ArrayList()
                for (i in 0 until size) {
                    val temp = allVisitPlaceList[i]

                    placeList.add(
                        Place(
                            null,
                            "",
                            Date(0),
                            "test 입니다 ",
                            null,
                            temp.lat,
                            temp.lng,
                            temp.address
                        )
                    )
                }

                val createTravelData = Travel(
                    null,
                    travelViewModel.selectLocationList,
                    Date(0),
                    Date(0),
                    placeList
                )

                coroutineScope {
                    travelViewModel.createTravel(createTravelData)
                }
            }


        }
    } // End of buttonEvents

    private fun createTravelResponseObserve() {
        travelViewModel.createTravelResponseLiveData.observe(viewLifecycleOwner) {

            when (it) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "createTravelResponseLiveData Success: ${it.data}")
                    Log.d(TAG, "createTravelResponseLiveData Success: ${it.message}")
                    showToast("데이터 통신 성공, 저장 성공")

                    // 성공하면 빠져나가기
                    findNavController().popBackStack()
                }

                is NetworkResult.Error -> {
                    Log.d(TAG, "createTravelResponseLiveData Error: ${it.data}")
                    Log.d(TAG, "createTravelResponseLiveData Error: ${it.message}")
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "createTravelResponseLiveData Loading")
                }
            }
        }
    } // End of createTravelResponseObserve

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
