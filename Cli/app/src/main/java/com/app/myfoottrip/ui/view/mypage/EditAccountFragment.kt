package com.app.myfoottrip.ui.view.mypage

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.Application
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Join
import com.app.myfoottrip.data.viewmodel.FcmViewModel
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.data.viewmodel.TokenViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentEditAccountBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.AlertDialog
import com.app.myfoottrip.ui.view.dialogs.EditNicknameDialog
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.ui.view.start.StartActivity
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import com.app.myfoottrip.util.showToastMessage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "EditAccountFragment_마이풋트립"

class EditAccountFragment : BaseFragment<FragmentEditAccountBinding>(
    FragmentEditAccountBinding::bind, R.layout.fragment_edit_account
) {

    private lateinit var mainActivity: MainActivity

    private val userViewModel by activityViewModels<UserViewModel>()
    private val navigationViewModel by activityViewModels<NavigationViewModel>()
    private lateinit var callback: OnBackPressedCallback

    private val fcmViewModel by activityViewModels<FcmViewModel>()
    private val tokenViewModel by activityViewModels<TokenViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
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

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        getAccessTokenByRefreshTokenResponseLiveDataObserver()
        getUserMyData()
    } // End of init


    // 유저정보 데이터 초기화
    private fun initUser(join: Join) {
        binding.apply {
            Log.d(TAG, "initUser: $join")
            //프로필 이미지
            if (join.profile_image.isNullOrEmpty()) {
                editProfileImageview.setPadding(55)
                Glide.with(this@EditAccountFragment).load(R.drawable.ic_my).fitCenter().into(editProfileImageview)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)))
            } else {
                Glide.with(this@EditAccountFragment)
                    .load(join.profile_image)
                    .skipMemoryCache(true).diskCacheStrategy(
                        DiskCacheStrategy.NONE
                    ).thumbnail(
                        Glide.with(this@EditAccountFragment).load(R.drawable.loading_image)
                            .centerCrop()
                    ).centerCrop().into(editProfileImageview)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
                )
            }
            tvMyNickname.text = join.nickname // 닉네임
            tvMyEmail.text = join.email // 아이디
        }
    } // End of initUser

    // 닉네임 변경 다이얼로그 생성
    private fun editNicknameInput() {
        val editDialog = EditNicknameDialog(object : EditNicknameDialog.OnClickListener {
            override fun onClick(dialog: EditNicknameDialog) {
                // val user = userViewModel.wholeMyData.value
                 val nickname = dialog.etEditNickname.text.toString()
                if (nickname.isEmpty()){
                    dialog.etEditNickname.error = "닉네임을 입력해 주세요."
                }else{
                    userViewModel.wholeMyData.value?.join?.profile_image = null
//                    userViewModel.wholeMyData.value?.join?.nickname = nickname
                    userViewModel.wholeUpdateUserData.nickname = nickname
                    updateUserResponseLiveDataObserver(dialog)
                    updateUser()
                }

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

    private fun getUserMyData() {
        CoroutineScope(Dispatchers.IO).launch {
            tokenViewModel.getUserData()
        }
    } // End of getUserMyData

    private fun getAccessTokenByRefreshTokenResponseLiveDataObserver() {
        tokenViewModel.getUserResponseLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    initUser(it.data!!.join)
                    val join = it.data!!.join
                    userViewModel.wholeUpdateUserData.username = join.username
                    userViewModel.wholeUpdateUserData.nickname = join.nickname
                    userViewModel.wholeUpdateUserData.age = join.age
                    userViewModel.wholeUpdateUserData.email = join.email
                    userViewModel.wholeUpdateUserData.password = join.password

                }
                is NetworkResult.Error -> {
                    // AccessToken을 통해서 유저 정보를 가져오기 실패했는지 파악해야됨.
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: 토큰 만료됨")
                    // RefreshToken을 통해서 AccessToken을 재발급
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: ${it.data}")
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: ${it.message}")
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: 로딩 중입니다")
                }
            }
        }
    } // End of getAccessTokenByRefreshTokenResponseLiveDataObserver

    private fun updateUser() {
        CoroutineScope(Dispatchers.IO).launch {
            userViewModel.userUpdate()
        }
    } // End of getUserMyData

    private fun updateUserResponseLiveDataObserver(dialog: EditNicknameDialog) {
        userViewModel.updateUserResponseLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    // refreshToken & accessToken
                    // savedInstance 저장하기
                    initUser(it.data!!)
                    dialog.dismiss()
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }

//    private fun updateUserResponseLiveDataObserver(dialog: EditNicknameDialog) {
//        userViewModel.updateUserResponseLiveData.observe(viewLifecycleOwner) {
//            when (it) {
//                is NetworkResult.Success -> {
//                    initUser(it.data!!)
//                    binding.root.showSnackBarMessage("닉네임 수정이 완료되었습니다.")
//                    dialog.dismiss()
//                }
//                is NetworkResult.Error -> {
//                    // AccessToken을 통해서 유저 정보를 가져오기 실패했는지 파악해야됨.
//                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: 토큰 만료됨")
//                    // RefreshToken을 통해서 AccessToken을 재발급
//                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: ${it.data}")
//                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: ${it.message}")
//                }
//                is NetworkResult.Loading -> {
//                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: 로딩 중입니다")
//                }
//            }
//        }
//    }
}