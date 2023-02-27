package com.app.myfoottrip.ui.view.mypage

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.model.Email
import com.app.myfoottrip.viewmodel.JoinViewModel
import com.app.myfoottrip.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentEditEmailBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.AlertDialog
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

private const val TAG = "EditEmailFragment_마이풋트립"

class EditEmailFragment : BaseFragment<FragmentEditEmailBinding>(
    FragmentEditEmailBinding::inflate
) {
    private val userViewModel by activityViewModels<UserViewModel>()
    private val joinViewModel by activityViewModels<JoinViewModel>()

    private lateinit var mainActivity: MainActivity
    private lateinit var callback: OnBackPressedCallback

    val emailValidation =
        "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

    var isUpdate = false

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
            // 뒤로가기
            ivCancelBtn.setOnClickListener { findNavController().popBackStack() }

            //이메일 입력
            etEditEmail.addTextChangedListener {
                emailValidation()
            }

            //이메일 인증 버튼 클릭
            btnEmailConfirm.setOnClickListener {
                emailIsUsed(etEditEmail.text.toString().trim())
            }

            //인증번호 입력
            etSixNumber.addTextChangedListener {
                emailConfirm(etSixNumber.text.toString().trim())
            }

            // 저장 다이얼로그 나오기
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
        isUsedEmailObserver()
        emailValidateCheckObserver()
        updateIdObserver()
    }

    //이메일 유효성 체크 메소드
    private fun emailValidation() {
        var email = binding.etEditEmail.text.toString().trim() //공백제거
        val e = Pattern.matches(emailValidation, email)
        //이메일이 비어있는 경우
        if (email.isEmpty()) {
            binding.etEditEmail.error = "이메일을 입력하세요."
            binding.btnEmailConfirm.isEnabled = false
            binding.btnEmailConfirm.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.gray_400))
            binding.etEditEmail.errorColor = ContextCompat.getColor(requireContext(),R.color.heart_color)
            return
        } else {
            //이메일 형태가 정상일 경우
            if (e) {
                binding.etEditEmail.error = null
                binding.btnEmailConfirm.isEnabled = true
                binding.btnEmailConfirm.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.main))
            } else {
                binding.etEditEmail.error = "이메일 형식이 올바르지 않습니다."
                binding.etEditEmail.errorColor = ContextCompat.getColor(requireContext(),R.color.heart_color)
                binding.btnEmailConfirm.isEnabled = false
                binding.btnEmailConfirm.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.gray_400))
            }
        }
    }

    private fun showDialog() {
        val dialog = AlertDialog(requireActivity() as AppCompatActivity)

        dialog.setOnOKClickedListener {
            binding.apply {
                isUpdate = true
                updateId(etEditEmail.text?.trim().toString())
            }
        }

        dialog.setOnCancelClickedListener { }

        dialog.show("아이디 변경", "아이디를 변경하시겠습니까?")
    }

    private fun emailIsUsed(emailId: String){
        binding.apply {
            etEditEmail.error = "잠시만 기다려 주세요."
            etEditEmail.errorColor = ContextCompat.getColor(requireContext(),R.color.main)
            etEditEmail.isEnabled = false
            btnEmailConfirm.isEnabled = false
        }

        CoroutineScope(Dispatchers.IO).launch {
            joinViewModel.emailUsedCheck(Email(emailId))
        }
    }

    private fun isUsedEmailObserver() {
        joinViewModel.userResponseLiveData.observe(viewLifecycleOwner) {
            binding.apply {

                when (it) {
                    is NetworkResult.Success -> {
                        if (it.data == true) {
                            // 사용중인 이메일
                            etEditEmail.error = "사용 중인 이메일입니다."
                            binding.etEditEmail.errorColor = ContextCompat.getColor(requireContext(),R.color.heart_color)
                            etEditEmail.isEnabled = true
                        }

                        if (it.data == false) {
                            // 사용가능한 이메일
                            etEditEmail.error = "인증번호를 입력해 주세요."
                            binding.etEditEmail.errorColor = ContextCompat.getColor(requireContext(),R.color.main)
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.d(TAG, "이메일 체크 Error: ${it.data}")
                        etEditEmail.isEnabled = true
                    }
                    is NetworkResult.Loading -> {
                    }
                }
            }
        }
    } // End of isUsedEmailObserver

    //이메일 인증
    private fun emailConfirm(confirmNumber: String){

        if (binding.etSixNumber.text.toString().length == 6){
            binding.etSixNumber.isEnabled = false
        }

        CoroutineScope(Dispatchers.IO).launch {
            joinViewModel.emailValidateCheck(Email(binding.etEditEmail.text.toString().trim(), confirmNumber))
        }
    }

    private fun emailValidateCheckObserver() {
        joinViewModel.emailValidateResponse.observe(viewLifecycleOwner) {
            binding.apply {
                when (it) {
                    is NetworkResult.Success -> {
                        if (etSixNumber.text!!.trim().length == 6){
                            if (it.data == true) {
                                etSixNumber.error = "인증 성공"
                                etEditEmail.error = ""
                                etSixNumber.errorColor = ContextCompat.getColor(requireContext(),R.color.main)
                                etSixNumber.isEnabled = false
                                etEditEmail.isEnabled = false
                                btnSave.isEnabled = true
                                btnSave.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.main))
                            }

                            // 인증 실패할 경우, 재인증 버튼으로 바뀌면서 인증번호 EditText 전부 텍스트 없어짐
                            if (it.data == false) {
                                etSixNumber.error = "인증 실패"
                                etEditEmail.error = ""
                                etSixNumber.errorColor = ContextCompat.getColor(requireContext(),R.color.heart_color)
                                etEditEmail.isEnabled = true
                                etSixNumber.isEnabled = true
                                btnEmailConfirm.isEnabled = true
                                btnSave.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.gray_400))
                            }
                        }
                    }
                    is NetworkResult.Error -> {
                    }
                    is NetworkResult.Loading -> {
                    }
                }
            }
        }
    } // End of emailValidateCheckObserver

    //회원 아이디 변경
    private fun updateId(emailId: String){
        CoroutineScope(Dispatchers.IO).launch {
            userViewModel.updateId(emailId)
        }
    }

    private fun updateIdObserver(){
        userViewModel.updateIdResponseLiveData.observe(viewLifecycleOwner){
            when (it) {
                is NetworkResult.Success -> {
                    if (it.data == 200) {
                        if (isUpdate) {
                            binding.etEditEmail.error = ""
                            binding.etEditEmail.errorColor = ContextCompat.getColor(requireContext(),R.color.main)
                            binding.root.showSnackBarMessage("아이디가 변경 되었습니다.")
                            findNavController().popBackStack()

                        }
                    }
                }

                is NetworkResult.Error -> {
                    binding.root.showSnackBarMessage("아이디 변경에 실패하였습니다.")
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "createTravelResponseLiveData Loading")
                }
            }
        }
    }
}