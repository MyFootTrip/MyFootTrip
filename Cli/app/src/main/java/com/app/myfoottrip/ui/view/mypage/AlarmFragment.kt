package com.app.myfoottrip.ui.view.mypage

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.FragmentAlarmBinding
import com.app.myfoottrip.databinding.FragmentMyTravelBinding
import com.app.myfoottrip.ui.base.BaseFragment


private const val TAG = "AlarmFragment_마이풋트립"

class AlarmFragment : BaseFragment<FragmentAlarmBinding>(
    FragmentAlarmBinding::bind, R.layout.fragment_alarm
){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.apply {
//
//        }
    }

}