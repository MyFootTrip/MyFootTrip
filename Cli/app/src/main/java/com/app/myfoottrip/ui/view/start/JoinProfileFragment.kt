package com.app.myfoottrip.ui.view.start

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.JoinViewModel
import com.app.myfoottrip.databinding.FragmentJoinProfileBinding
import com.app.myfoottrip.util.ChangeMultipartUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

private const val TAG = "JoinProfileFragment_싸피"

class JoinProfileFragment : Fragment() {
    private lateinit var mContext: Context
    private lateinit var binding: FragmentJoinProfileBinding
    private lateinit var joinBackButtonCustomView: JoinBackButtonCustomView
    private val joinViewModel by activityViewModels<JoinViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinProfileBinding.inflate(inflater, container, false)
        return binding.root
    } // End of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 프로필 이미지 추가 버튼 클릭
        binding.joinProfileImagePlusButton.setOnClickListener {
            selectGallery()
        }

        joinBackButtonCustomView = binding.joinBackButtonCustomview
        joinBackButtonCustomView.findViewById<AppCompatButton>(R.id.custom_back_button_appcompatbutton)
            .setOnClickListener {
                findNavController().popBackStack()
            }


        // 연령대 선택 프래그먼트로 이동
        binding.joinNextButton.setOnClickListener {
            joinViewModel.wholeJoinUserData.nickname =
                binding.editTextJoinNickname.text.toString()

            joinViewModel.wholeJoinUserData.username =
                binding.editTextJoinName.text.toString()

            Navigation.findNavController(binding.joinNextButton)
                .navigate(R.id.action_joinProfileFragment_to_joinAgeFragment)
        }

        // 회원가입 프로필 이미지 옵저버
        userProfileImageObserver()
    } // End of onViewCreated

    private fun selectGallery() {
        val writePermission = ContextCompat.checkSelfPermission(
            mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            mContext, android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                mContext as Activity, arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), REQ_GALLERY
            )
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*"
            )
            imageResult.launch(intent)
        }
    }

    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data ?: return@registerForActivityResult
            joinViewModel.setUserImageUri(imageUri)
        }
    } // End of registerForActivityResult

    private fun userProfileImageObserver() {
        joinViewModel.userProfileImage.observe(viewLifecycleOwner) {
            binding.joinProfileImageview.setImageURI(it)

            // 이미지 Uri를 Multipart로 변경해서 전송하기위한 코드
            val file = File(
                ChangeMultipartUtil().changeAbsolutelyPath(
                    joinViewModel.userProfileImage.value,
                    mContext
                )
            )
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val body = MultipartBody.Part.createFormData("profileImg", file.name, requestFile)
            joinViewModel.setUserImageUriToMultipart(body)
            Log.d(TAG, "userProfileImageObserver: $")
        }
    } // End of userProfileImageObserver

    companion object {
        const val REQ_GALLERY = 1
    }

} // End of JoinProfileFragment class
