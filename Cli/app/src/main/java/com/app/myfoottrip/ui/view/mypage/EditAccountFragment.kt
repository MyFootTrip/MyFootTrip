package com.app.myfoottrip.ui.view.mypage

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentEditAccountBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.EditNicknameDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

private const val TAG = "EditAccountFragment_마이풋트립"

class EditAccountFragment : BaseFragment<FragmentEditAccountBinding>(
    FragmentEditAccountBinding::bind, R.layout.fragment_edit_account
) {

    private val userViewModel by activityViewModels<UserViewModel>()
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

        init()

        binding.apply {
            // 뒤로가기
            ivBack.setOnClickListener {
                navigationViewModel.type = 1
                findNavController().popBackStack()}

            // 닉네임 변경 다이얼로그 나오기
            chipEditAccount.setOnClickListener {
                val dialog = EditNicknameDialog()
                dialog.show(activity?.supportFragmentManager!!, "EditNicknameDialog")
            }

            // 아이디 변경 페이지로 이동
            chipEditEmail.setOnClickListener {
                findNavController().navigate(R.id.action_editAccountFragment_to_editEmailFragment)
            }

            // 비밀번호 변경 페이지로 이동
            chipEditPassword.setOnClickListener {
                findNavController().navigate(R.id.action_editAccountFragment_to_editPasswordFragment)
            }

        }

    } // End of onViewCreated

    private fun init() {
        initUser()
    } // End of init

    // 유저정보 데이터 초기화
    private fun initUser(){
        binding.apply {
            //프로필 이미지
            if (userViewModel.wholeMyData.value?.join?.profile_image.isNullOrEmpty()){
                editProfileImageview.setPadding(55)
                Glide.with(this@EditAccountFragment).load(R.drawable.ic_my).fitCenter().into(editProfileImageview)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white)))
            }else {
                Glide.with(this@EditAccountFragment).load(userViewModel.wholeMyData.value?.join?.profile_image).skipMemoryCache(true).diskCacheStrategy(
                    DiskCacheStrategy.NONE).thumbnail(Glide.with(this@EditAccountFragment).load(R.drawable.loading_image).centerCrop()).centerCrop().into(editProfileImageview)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white)))
            }

            tvMyNickname.text = "${userViewModel.wholeMyData.value?.join?.nickname}" // 닉네임
            tvMyEmail.text = "${userViewModel.wholeMyData.value?.join?.email}" // 아이디
        }
    } // End of initUser

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    } // End of onDetach
}