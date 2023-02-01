package com.app.myfoottrip.ui.view.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.Navigation
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.FragmentMyPageBinding
import com.app.myfoottrip.ui.base.BaseFragment

private const val TAG = "MyPageFragment_마이풋트립"

class MyPageFragment : BaseFragment<FragmentMyPageBinding>(
    FragmentMyPageBinding::bind, R.layout.fragment_my_page
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // 내 여정 페이지로 이동
            cvMyTravel.setOnClickListener {
                Navigation.findNavController(binding.cvMyTravel)
                    .navigate(R.id.action_mainFragment_to_myTravelFragment)
            }
            // 개인정보수정 페이지로 이동
            llToEditAccount.setOnClickListener{
                Navigation.findNavController(binding.llToEditAccount)
                    .navigate(R.id.action_mainFragment_to_editAccountFragment)
            }
        }
    }

}