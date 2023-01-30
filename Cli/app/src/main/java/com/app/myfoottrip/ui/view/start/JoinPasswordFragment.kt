package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.JoinViewModel
import com.app.myfoottrip.databinding.FragmentJoinPasswordBinding
import com.google.android.material.textfield.TextInputEditText


private const val TAG = "싸피"

class JoinPhoneNumberFragment : Fragment() {
    private lateinit var mContext: Context
    private lateinit var binding: FragmentJoinPasswordBinding
    private lateinit var customViewLayout: JoinCustomView
    private lateinit var joinBackButtonCustomView: JoinBackButtonCustomView
    private val joinViewModel by activityViewModels<JoinViewModel>()

    private lateinit var passwordConfirm: TextInputEditText
    private lateinit var nextButton: AppCompatButton
    private lateinit var secondPasswordInformMessageTv: TextView
    private lateinit var pwOrigin: TextInputEditText
    private lateinit var firstPasswordInformMessageTv: TextView


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    } // End of onCreate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinPasswordBinding.inflate(inflater, container, false)
        return binding.root
    } // End of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        customViewLayout = binding.joinCustomViewLayout
        joinBackButtonCustomView = customViewLayout.findViewById(R.id.join_back_button_customview)
        customViewDataInit()

        joinBackButtonCustomView.findViewById<AppCompatButton>(R.id.custom_back_button_appcompatbutton)
            .setOnClickListener {
                Log.d(TAG, "joinBackButtonCustomView : onClick")
                findNavController().popBackStack()
            }

        // 커스텀 뷰 2번으로 설정해서 비밀번호 화면이 보이도록 설정
        customViewLayout.setView(2)


        customViewLayout.findViewById<AppCompatButton>(R.id.emailConfirmButton).setOnClickListener {
            // 인증 버튼을 눌러서 인증번호 보내는 요청이 successful이 되면 밑에 인증번호를 적는 화면이 보이게 됨
            // joinViewModel.phoneNumberUseValidation()
        }

        customViewLayout.findViewById<AppCompatButton>(R.id.join_next_button).setOnClickListener {
            joinViewModel.wholeJoinUserData.password = pwOrigin.text.toString()

            Navigation.findNavController(customViewLayout.findViewById<AppCompatButton>(R.id.join_next_button))
                .navigate(R.id.action_joinPasswordFragment_to_joinProfileFragment)
        }


        // 각 EditText가 포커싱에서 벗어났을 때 에러메시지를 띄움
        pwOrigin.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                firstPasswordInformMessageTv.text = emailValidCheck()
            }
        }

        passwordConfirm.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                joinViewModel.setPwLiveData(
                    pwOrigin.text.toString(),
                    passwordConfirm.text.toString()
                )
            }
        }

        passwordEqualCheckObserver()
    } // End of onViewCreated

    private fun customViewDataInit() {
        passwordConfirm = customViewLayout.findViewById(R.id.secondJoinEd)
        nextButton = customViewLayout.findViewById(R.id.join_next_button)
        secondPasswordInformMessageTv = customViewLayout.findViewById(R.id.secondEditTextMessageTv)
        pwOrigin = customViewLayout.findViewById(R.id.editTextJoinEmail)
        firstPasswordInformMessageTv = customViewLayout.findViewById(R.id.firstEditTextMessageTv)
    } // End of customViewDataInit

    override fun onResume() {
        super.onResume()
        customViewLayout.findViewById<AppCompatButton>(R.id.emailConfirmButton).isClickable = false
        nextButton.isClickable = false
        nextButton.isEnabled = false
    }

    private fun passwordEqualCheckObserver() {
        joinViewModel.confirmPassword.observe(viewLifecycleOwner) {
            if (joinViewModel.originPassword.value == joinViewModel.confirmPassword.value) {
                nextButton.isClickable = true
                nextButton.isEnabled = true
                secondPasswordInformMessageTv.text = ""
            } else {
                secondPasswordInformMessageTv.text = "비밀번호가 다릅니다."
                nextButton.isClickable = false
                nextButton.isEnabled = false
            }
        }
    } // End of passwordEqualCheckObserver

    private fun emailValidCheck(): String {
        val len = pwOrigin.text!!.length

        if (len < 8) return "비밀번호는 8자리 이상이어야 합니다."

        if (len > 15) return "비밀번호는 15자리 이하이어야 합니다."

        if (isPasswordValid(pwOrigin.text.toString()) == false) return "숫자, 문자, 특수문자 모두 포함이 되어야 합니다."

        // 모든 조건 통과
        return "사용가능"
    } // End of emailValidCheck

    /*
        비밀번호 규칙
        1. 8자리 이상 15자리 이하.
        2. 숫자, 문자, 특수문자 모두 포함
     */
    private companion object {
        @JvmStatic
        val PASSWORD_REGEX = """^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^+\-=])(?=\S+$).*$"""
        fun isPasswordValid(pw: String): Boolean {
            return PASSWORD_REGEX.toRegex().matches(pw)
        } // End of isPasswordValid
    } // End of private companion object

} // End of JoinPhoneNumberFragment class
