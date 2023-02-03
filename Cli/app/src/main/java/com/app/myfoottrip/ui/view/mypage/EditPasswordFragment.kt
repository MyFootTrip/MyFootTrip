package com.app.myfoottrip.ui.view.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentEditPasswordBinding
import com.app.myfoottrip.ui.base.BaseFragment

private const val TAG = "EditPasswordFragment_마이풋트립"

class EditPasswordFragment : BaseFragment<FragmentEditPasswordBinding>(
    FragmentEditPasswordBinding::bind, R.layout.fragment_edit_password
) {
    private val userViewModel by activityViewModels<UserViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            ivCancelBtn.setOnClickListener {findNavController().popBackStack()} //뒤로가기

        }

    } // End of onViewCreated
}