package com.app.myfoottrip.ui.view.mypage

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.databinding.FragmentMyLikeBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity


class MyLikeFragment : BaseFragment<FragmentMyLikeBinding>(
    FragmentMyLikeBinding::bind, R.layout.fragment_my_like
) {

    private lateinit var mainActivity: MainActivity

    private val navigationViewModel by activityViewModels<NavigationViewModel>()
    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mainActivity = context as MainActivity

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigationViewModel.type = 1
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this@MyLikeFragment, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            // 뒤로가기
            ivBack.setOnClickListener {
                navigationViewModel.type = 1
                findNavController().popBackStack()
            }
        }

    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }
}