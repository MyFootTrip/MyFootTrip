package com.app.myfoottrip.ui.view.travel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.FragmentTravelStartBinding
import com.app.myfoottrip.ui.base.BaseFragment


private const val TAG = "TravelStartFragment_싸피"
class TravelStartFragment :BaseFragment<FragmentTravelStartBinding>(
    FragmentTravelStartBinding::bind, R.layout.fragment_travel_start
){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }

    private fun initialize(){

    }

    private fun initMap(){

    }
}