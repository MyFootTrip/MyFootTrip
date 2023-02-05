package com.app.myfoottrip.ui.view.dialogs;

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.DialogEditNicknameBinding
import com.app.myfoottrip.util.DeviceSizeUtil

class EditNicknameDialog() : DialogFragment() {

    private val userViewModel by activityViewModels<UserViewModel>()
    private var _binding: DialogEditNicknameBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var size: Point

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogEditNicknameBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        size = DeviceSizeUtil.deviceSizeCheck(mContext)

        return binding.root
    } // End of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 갑자기 빨간 불 떠서 다시 수정 각~~~
        binding.apply {
            // 취소 버튼 클릭
            //ivCancelBtn.setOnClickListener { dismiss() }

            // 현재 유저 닉네임 가져오기
            //etEditNickname.setText("${userViewModel.wholeMyData.value?.join?.nickname}")

            // 변경된 유져 닉네임 저장

        }
    } // End of onViewCreated

    private fun init() {
        initUser()
    } // End of init

    // 유저정보 데이터 초기화
    private fun initUser(){
        binding.apply {
        }
    } // End of initUser

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    } // End of onDestroyView

} // End of EditNicknameDialog
