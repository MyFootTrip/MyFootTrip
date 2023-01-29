package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.FragmentEditSaveTravelBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.naver.maps.map.MapView

class EditSaveTravelFragment : BaseFragment<FragmentEditSaveTravelBinding>(
    FragmentEditSaveTravelBinding::bind, R.layout.fragment_edit_save_travel
) {

    lateinit var mapView : MapView
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    } // End of onCreate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapFragment


        // 버튼 클릭시 일어나는 모든 이벤트를 관리하는 메소드
        buttonEvents()
    } // End of onViewCreated

    private fun buttonEvents() {
        // 저장 버튼 눌렀을 때 이벤트
        binding.travelEditSaveButton.setOnClickListener {


            // 수정된 데이터들을 모두 반영해서 저장한 뒤에 밖으로 나감
            findNavController().popBackStack()
        }
    } // End of buttonEvents

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
