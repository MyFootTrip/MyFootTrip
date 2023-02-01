package com.app.myfoottrip.ui.view.mypage

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.databinding.FragmentEditAccountBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.start.StartActivity

private const val TAG = "EditAccountFragment_마이풋트립"

class EditAccountFragment : BaseFragment<FragmentEditAccountBinding>(
    FragmentEditAccountBinding::bind, R.layout.fragment_edit_account
) {

    private val navigationViewModel by activityViewModels<NavigationViewModel>()
    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigationViewModel.type = 1
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // 뒤로가기
            ivBack.setOnClickListener {
                navigationViewModel.type = 1
                findNavController().popBackStack()}

            // 닉네임 변경 다이얼로그 나오기
            chipEditAccount.setOnClickListener {
                val mainAct = requireActivity() as StartActivity
                mainAct.showServiceDialog()
            }


        }

    } // End of onViewCreated

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }
}