package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.databinding.FragmentFirstBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.splash.SplashFragment

private const val TAG = "FirstFragment_마이풋트립"
class FirstFragment : BaseFragment<FragmentFirstBinding>(
    FragmentFirstBinding::bind, R.layout.fragment_first
) {
    private lateinit var mContext: Context
    private val navigationViewModel by activityViewModels<NavigationViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNavigation()
    }

    //바텀 네비게이션 설정
    private fun initNavigation() {
        binding.apply {
            when(navigationViewModel.startPage){
                0 -> {parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_start, SplashFragment()).commit()}
                1 -> {parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_start, StartFragment()).commit()}
            }
        }
    }
}