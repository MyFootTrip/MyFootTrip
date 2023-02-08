package com.app.myfoottrip.ui.view.mypage

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.JoinViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentEditPasswordBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.AlertDialog
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

private const val TAG = "EditPasswordFragment_마이풋트립"

class EditPasswordFragment : BaseFragment<FragmentEditPasswordBinding>(
    FragmentEditPasswordBinding::bind, R.layout.fragment_edit_password
) {
    private val userViewModel by activityViewModels<UserViewModel>()
    private val joinViewModel by activityViewModels<JoinViewModel>()

    private lateinit var mainActivity: MainActivity
    private lateinit var callback: OnBackPressedCallback

    private var isUpdate = false

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

        binding.apply {

            ivCancelBtn.setOnClickListener {findNavController().popBackStack()} //뒤로가기

            etEditNewPassword.addTextChangedListener {
                checkPassword()
            }

            etEditNewPasswordCheck.addTextChangedListener {
                checkConfirm()
            }

            //변경하기 버튼
            btnSave.setOnClickListener {
                showDialog()
            }
        }

    } // End of onViewCreated

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    } // End of onDetach

    private fun initObserver(){
        updatePassObserver()
    }

    private fun showDialog() {
        val dialog = AlertDialog(requireActivity() as AppCompatActivity)

        dialog.setOnOKClickedListener {
            binding.apply {
                isUpdate = true
                val password = etEditNewPassword.text.toString().trim()
                val passwordConfirm = etEditNewPasswordCheck.text.toString().trim()
                updatePass(password, passwordConfirm)
            }
        }

        dialog.setOnCancelClickedListener { }

        dialog.show("비밀번호 변경", "비밀번호를 변경하시겠습니까?")
    }

    //비밀번호 유효성 체크 메소드
    private fun checkPassword() {
        binding.apply {
            var password = etEditNewPassword.text.toString().trim()
            var confirm = etEditNewPasswordCheck.text.toString().trim()
            if (password.isEmpty()) {
                etEditNewPassword.error = "비밀번호는 대문자, 소문자, 숫자, 특수문자 중 2종류 이상을 조합하여 8자리 이상 입력해주세요."
                etEditNewPassword.errorColor = ContextCompat.getColor(requireContext(),R.color.heart_color)
                btnSave.isEnabled = false
                btnSave.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.gray_400))
                return
            } else {
                if (!Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,20}$", password)
                ) {
                    //비밀번호 형태가 정상일 경우
                    etEditNewPassword.error = "비밀번호는 대문자, 소문자, 숫자, 특수문자 중 2종류 이상을 조합하여 8자리 이상 입력해주세요."
                    etEditNewPassword.errorColor = ContextCompat.getColor(requireContext(),R.color.heart_color)
                    btnSave.isEnabled = false
                    btnSave.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.gray_400))

                    if (confirm.isNotEmpty()){
                        if (password != confirm){
                            etEditNewPasswordCheck.error = "비밀번호와 일치하지 않습니다."
                            etEditNewPasswordCheck.errorColor = ContextCompat.getColor(requireContext(),R.color.heart_color)
                        }else{
                            etEditNewPasswordCheck.error = "비밀번호와 일치합니다."
                            etEditNewPasswordCheck.errorColor = ContextCompat.getColor(requireContext(),R.color.main)
                        }
                    }
                    return
                } else {
                    etEditNewPassword.error = "사용가능한 비밀번호입니다."
                    etEditNewPassword.errorColor = ContextCompat.getColor(requireContext(),R.color.main)

                    if (confirm.isNotEmpty()){
                        if (password != confirm){
                            etEditNewPasswordCheck.error = "비밀번호와 일치하지 않습니다."
                            etEditNewPasswordCheck.errorColor = ContextCompat.getColor(requireContext(),R.color.heart_color)
                            btnSave.isEnabled = false
                            btnSave.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.gray_400))
                        }else{
                            etEditNewPasswordCheck.error = "비밀번호와 일치합니다."
                            etEditNewPasswordCheck.errorColor = ContextCompat.getColor(requireContext(),R.color.main)
                            btnSave.isEnabled = true
                            btnSave.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.main))
                        }
                    }
                    return
                }
            }
        }
    }

    //비밀번호 확인 메소드
    private fun checkConfirm(){
        binding.apply {
            var confirm = etEditNewPasswordCheck.text.toString().trim()
            val password = etEditNewPassword.text.toString().trim()

            if (confirm.isNotEmpty()){
                if (confirm == password){
                    etEditNewPasswordCheck.error = "비밀번호와 일치합니다."
                    etEditNewPasswordCheck.errorColor = ContextCompat.getColor(requireContext(),R.color.main)
                    btnSave.isEnabled = true
                    btnSave.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.main))
                }else{
                    etEditNewPasswordCheck.error = "비밀번호와 일치하지 않습니다."
                    etEditNewPasswordCheck.errorColor = ContextCompat.getColor(requireContext(),R.color.heart_color)
                    btnSave.isEnabled = false
                    btnSave.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.gray_400))
                }
            }
        }
    }

    //비밀번호 변경
    private fun updatePass(password: String, passwordConfirm: String){
        CoroutineScope(Dispatchers.IO).launch {
            userViewModel.updatePass(password, passwordConfirm)
        }
    }

    private fun updatePassObserver(){
        userViewModel.updatePassResponseLiveData.observe(viewLifecycleOwner){
            when (it) {
                is NetworkResult.Success -> {
                    if (it.data == 200) {
                        if (isUpdate) {
                            binding.etEditNewPassword.error = ""
                            binding.etEditNewPasswordCheck.errorColor = ContextCompat.getColor(requireContext(),R.color.main)
                            binding.etEditNewPasswordCheck.error = ""
                            binding.etEditNewPasswordCheck.errorColor = ContextCompat.getColor(requireContext(),R.color.main)
                            binding.root.showSnackBarMessage("비밀번호가 변경 되었습니다.")
                            findNavController().popBackStack()

                        }
                    }else if(it.data == 400){
                        if (isUpdate) {
                            binding.etEditNewPassword.error = "비밀번호와 비밀번호가 일치하지 않습니다."
                            binding.etEditNewPasswordCheck.errorColor = ContextCompat.getColor(requireContext(),R.color.heart_color)
                            binding.etEditNewPasswordCheck.error = "비밀번호와 비밀번호가 일치하지 않습니다."
                            binding.etEditNewPasswordCheck.errorColor = ContextCompat.getColor(requireContext(),R.color.heart_color)
                            binding.root.showSnackBarMessage("비밀번호가 변경 되었습니다.")
                            findNavController().popBackStack()
                        }
                    }
                }

                is NetworkResult.Error -> {
                    binding.root.showSnackBarMessage("비밀번호 변경에 실패하였습니다.")
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "createTravelResponseLiveData Loading")
                }
            }
        }
    }
}