package com.app.myfoottrip.ui.view.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.FragmentMyPageBinding
import com.app.myfoottrip.ui.base.BaseFragment


class MyPageFragment : BaseFragment<FragmentMyPageBinding>(
    FragmentMyPageBinding::bind, R.layout.fragment_my_page
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}