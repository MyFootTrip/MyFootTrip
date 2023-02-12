package com.app.myfoottrip.ui.view.mypage

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.JoinViewModel
import com.app.myfoottrip.data.viewmodel.TokenViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentEditProfileImageBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.AlertDialog
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.ChangeMultipartUtil
import com.app.myfoottrip.util.GalleryUtils
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

private const val TAG = "EditProfileImageFragmen_마이풋트립"
class EditProfileImageFragment : BaseFragment<FragmentEditProfileImageBinding>(
    FragmentEditProfileImageBinding::inflate
) {
    private lateinit var mainActivity: MainActivity
    private lateinit var callback: OnBackPressedCallback

    private val joinViewModel by activityViewModels<JoinViewModel>()
    private val tokenViewModel by activityViewModels<TokenViewModel>()
    private val userViewModel by activityViewModels<UserViewModel>()

    private var isUpdate = false

    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data ?: return@registerForActivityResult
            userViewModel.setUserImageUri(imageUri)
        }
    } // End of registerForActivityResult

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    } // End of onAttach

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        getUserMyData()

        binding.apply {
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }

            //프로필 이미지 버튼 클릭시
            editProfileImageLayout.setOnClickListener {
                GalleryUtils.getGallery(requireContext(), imageResult)
            }

            //이미지 변경 버튼 클릭시
            tvUpdate.setOnClickListener {
                showDialog()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    } // End of onDetach

    // 유저정보 데이터 초기화
    private fun initProfile() {
        binding.apply {
            //프로필 이미지
            if (userViewModel.wholeMyData.value?.join?.profile_image.isNullOrEmpty()) {
                editProfileImageview.setPadding(55)
                Glide.with(this@EditProfileImageFragment).load(R.drawable.ic_my).fitCenter().into(editProfileImageview)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)))
            } else {
                Glide.with(this@EditProfileImageFragment).load(userViewModel.wholeMyData.value?.join?.profile_image).thumbnail(Glide.with(this@EditProfileImageFragment).load(R.drawable.loading_image).centerCrop()).centerCrop().into(editProfileImageview)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)))
            }
            tvMyNickname.text = userViewModel.wholeMyData.value?.join?.nickname // 닉네임
            tvMyEmail.text = userViewModel.wholeMyData.value?.join?.email // 아이디
        }
    } // End of initUser

    private fun initObserver(){
        userProfileImageObserver()
        getAccessTokenByRefreshTokenResponseLiveDataObserver()
        updateUserResponseLiveDataObserver()
    }

    private fun showDialog() {
        val dialog = AlertDialog(requireActivity() as AppCompatActivity)

        dialog.setOnOKClickedListener {
            binding.apply {
                binding.editAccountLayout.visibility = View.GONE
                lottieProfileImage.visibility = View. VISIBLE
                tvWait.visibility = View.VISIBLE
                //화면 터치 막기
                mainActivity.window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                isUpdate = true
                updateProfileImg()
            }
        }

        dialog.setOnCancelClickedListener { }

        dialog.show("프로필이미지 변경", "이미지를 변경하시겠습니까?")
    }

    private fun userProfileImageObserver() {
        userViewModel.userProfileImage.observe(viewLifecycleOwner) {
            Glide.with(this@EditProfileImageFragment).load(it).into(binding.editProfileImageview)

            // 이미지 Uri를 Multipart로 변경해서 전송하기위한 코드
            val file = File(
                ChangeMultipartUtil().changeAbsolutelyPath(
                    userViewModel.userProfileImage.value,
                    mainActivity
                )
            )
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val body = MultipartBody.Part.createFormData("profile_image", file.name, requestFile)
            userViewModel.setUserImageUriToMultipart(body)
        }
    } // End of userProfileImageObserver

    private fun getUserMyData() {
        CoroutineScope(Dispatchers.IO).launch {
            tokenViewModel.getUserData()
        }
    } // End of getUserMyData

    private fun getAccessTokenByRefreshTokenResponseLiveDataObserver() {
        tokenViewModel.getUserResponseLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    userViewModel.setWholeMyData(it.data!!)
                    initProfile()
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

    private fun updateProfileImg(){
        CoroutineScope(Dispatchers.IO).launch {
            userViewModel.userUpdate()
        }
    }

    private fun updateUserResponseLiveDataObserver() {
        userViewModel.updateUserResponseLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    // refreshToken & accessToken
                    // savedInstance 저장하기
                    if (isUpdate){
                        binding.editAccountLayout.visibility = View.VISIBLE
                        binding.lottieProfileImage.visibility = View.GONE
                        binding.tvWait.visibility = View.GONE
                        //화면 터치 풀기
                        mainActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        binding.root.showSnackBarMessage("이미지 변경이 완료되었습니다.")
                        userViewModel.wholeMyData.value?.join = it.data!!
                        findNavController().popBackStack()
                    }
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }
}