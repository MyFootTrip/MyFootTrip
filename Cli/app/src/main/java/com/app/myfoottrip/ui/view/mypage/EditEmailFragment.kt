package com.app.myfoottrip.ui.view.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentEditAccountBinding
import com.app.myfoottrip.databinding.FragmentEditEmailBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.EditNicknameDialog
import com.app.myfoottrip.ui.view.dialogs.EditSaveDialog

private const val TAG = "EditEmailFragment_마이풋트립"

class EditEmailFragment : BaseFragment<FragmentEditEmailBinding>(
    FragmentEditEmailBinding::bind, R.layout.fragment_edit_email
) {
    private val userViewModel by activityViewModels<UserViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // 뒤로가기
            ivCancelBtn.setOnClickListener {findNavController().popBackStack()}

            // 현재 이메일 가져오기
            etEditEmail.setText("${userViewModel.wholeMyData.value?.join?.email}")

            // 저장 다이얼로그 나오기
            btnSave.setOnClickListener {
                val dialog = EditSaveDialog()
                dialog.isCancelable = false // 알림창이 띄워져있는 동안 배경 클릭 막기
                dialog.show(activity?.supportFragmentManager!!, "EditSaveDialog")
            }
        }

    } // End of onViewCreated
}