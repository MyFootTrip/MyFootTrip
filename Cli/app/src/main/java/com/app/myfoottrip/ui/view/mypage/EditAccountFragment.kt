package com.app.myfoottrip.ui.view.mypage

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.Application
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.FcmViewModel
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.data.viewmodel.TokenViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentEditAccountBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.AlertDialog
import com.app.myfoottrip.ui.view.dialogs.EditNicknameDialog
import com.app.myfoottrip.ui.view.start.StartActivity
import com.app.myfoottrip.util.GalleryUtils
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "EditAccountFragment_마이풋트립"

class EditAccountFragment : BaseFragment<FragmentEditAccountBinding>(
    FragmentEditAccountBinding::bind, R.layout.fragment_edit_account
) {

    private val userViewModel by activityViewModels<UserViewModel>()
    private val navigationViewModel by activityViewModels<NavigationViewModel>()
    private lateinit var callback: OnBackPressedCallback

    private val fcmViewModel by activityViewModels<FcmViewModel>()
    private val tokenViewModel by activityViewModels<TokenViewModel>()

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
                findNavController().popBackStack()
            }

            // 닉네임 변경 다이얼로그 나오기
            chipEditAccount.setOnClickListener { editNicknameInput() }
//            chipEditAccount.setOnClickListener {
//                val dialog = EditNicknameDialog()
//                dialog.show(activity?.supportFragmentManager!!, "EditNicknameDialog")
//            }

            // 아이디 변경 페이지로 이동
            chipEditEmail.setOnClickListener {
                findNavController().navigate(R.id.action_editAccountFragment_to_editEmailFragment)
            }

            // 비밀번호 변경 페이지로 이동
            chipEditPassword.setOnClickListener {
                findNavController().navigate(R.id.action_editAccountFragment_to_editPasswordFragment)
            }

            //로그아웃
            tvLogout.setOnClickListener {
                showDialog()
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
    private fun initUser() {
        binding.apply {
            //프로필 이미지
            if (userViewModel.wholeMyData.value?.join?.profile_image.isNullOrEmpty()) {
                editProfileImageview.setPadding(55)
                Glide.with(this@EditAccountFragment).load(R.drawable.ic_my).fitCenter().into(editProfileImageview)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)))
            } else {
                Glide.with(this@EditAccountFragment)
                    .load(userViewModel.wholeMyData.value?.join?.profile_image)
                    .skipMemoryCache(true).diskCacheStrategy(
                        DiskCacheStrategy.NONE
                    ).thumbnail(
                        Glide.with(this@EditAccountFragment).load(R.drawable.loading_image)
                            .centerCrop()
                    ).centerCrop().into(editProfileImageview)
                cvProfileLayout.setCardBackgroundColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                )
            }

            tvMyNickname.text = "${userViewModel.wholeMyData.value?.join?.nickname}" // 닉네임
            tvMyEmail.text = "${userViewModel.wholeMyData.value?.join?.email}" // 아이디
        }
    } // End of initUser

    // 닉네임 변경 다이얼로그 생성
    private fun editNicknameInput() {
        val editDialog = EditNicknameDialog(object : EditNicknameDialog.OnClickListener {
            override fun onClick(dialog: EditNicknameDialog) {
                // val user = userViewModel.wholeMyData.value
                // val nickname = dialog.etEditNickname.text.toString()
                dialog.dismiss()
            }
        })

        editDialog.show(parentFragmentManager, editDialog.mTag)
    } // End of editNickname

    // private fun editNicknameObserver()

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    } // End of onDetach

    //로그아웃 다이얼로그 생성
    private fun showDialog() {
        val dialog = AlertDialog(requireActivity() as AppCompatActivity)

        dialog.setOnOKClickedListener {
            binding.apply {
                deleteRefreshTokenObserver()
                deleteRefreshToken()
            }
        }

        dialog.setOnCancelClickedListener { }

        dialog.show("로그아웃", "로그아웃 하시겠습니까?")
    }

    //refresh 토큰 삭제하기
    private fun deleteRefreshToken() {
        CoroutineScope(Dispatchers.IO).launch {
            tokenViewModel.deleteRefreshToken(Application.sharedPreferencesUtil.getUserRefreshToken())
        }
    }

    private fun deleteRefreshTokenObserver() {
        tokenViewModel.deleteRefreshTokenResponseLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    deleteFcmTokenObserver()
                    deleteFcmToken()
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }

    //FCM 토큰 삭제하기
    private fun deleteFcmToken() {
        CoroutineScope(Dispatchers.IO).launch {
            fcmViewModel.deleteFcmToken(Application.sharedPreferencesUtil.getFcmToken())
        }
    }

    private fun deleteFcmTokenObserver() {
        fcmViewModel.deleteFcmToken.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    val intent = Intent(activity, StartActivity::class.java)
                    intent.putExtra("page", 1)
                    Application.sharedPreferencesUtil.deleteAccessToken()
                    Application.sharedPreferencesUtil.deleteRefreshToken()
                    startActivity(intent)
                    binding.root.showSnackBarMessage("로그아웃 하셨습니다.")
                    activity!!.finish()
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }
}