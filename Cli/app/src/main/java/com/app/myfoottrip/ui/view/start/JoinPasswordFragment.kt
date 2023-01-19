package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.app.myfoottrip.R
import com.app.myfoottrip.data.model.viewmodel.JoinViewModel
import com.app.myfoottrip.databinding.FragmentJoinPasswordBinding
import com.app.myfoottrip.util.showToastMessage
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


private const val TAG = "싸피"

class JoinPhoneNumberFragment : Fragment() {
    private lateinit var mContext: Context
    private lateinit var binding: FragmentJoinPasswordBinding
    private lateinit var customViewLayout: JoinCustomView
    private val joinViewModel by activityViewModels<JoinViewModel>()

    private lateinit var secondJoinEd: TextInputEditText
    private lateinit var nextButton: AppCompatButton
    private lateinit var passwordConfirm: TextInputEditText
    private lateinit var secondEditTextMessageTv: TextView
    private lateinit var pwOrigin: TextInputEditText


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
        customViewDataInit()

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

        pwOrigin.addTextChangedListener {
            joinViewModel.setPwLiveData(pwOrigin.text.toString(), secondJoinEd.text.toString())
        }

        secondJoinEd.addTextChangedListener {
            joinViewModel.setPwLiveData(pwOrigin.text.toString(), secondJoinEd.text.toString())
        }


        // 전화번호 인증 성공여부 옵저버
        phoneNumberValidObserver()

        passwordEqualCheckObserver()
    } // End of onViewCreated

    private fun customViewDataInit() {
        secondJoinEd = customViewLayout.findViewById(R.id.secondJoinEd)
        nextButton = customViewLayout.findViewById(R.id.join_next_button)
        secondEditTextMessageTv = customViewLayout.findViewById(R.id.secondEditTextMessageTv)
        pwOrigin = customViewLayout.findViewById(R.id.editTextJoinEmail)
    } // End of customViewDataInit

    override fun onResume() {
        super.onResume()
        customViewLayout.findViewById<AppCompatButton>(R.id.emailConfirmButton).isClickable = false
        nextButton.isClickable = false
        nextButton.isEnabled = false
    }

    private fun phoneNumberValidObserver() {
        joinViewModel.phoneNumberValidation.observe(viewLifecycleOwner) {
            if (joinViewModel.phoneNumberValidation.value == true) {
                certifyViewSet()
            }
        }
    } // End of phoneNumberValidObserver

    private fun certifyViewSet() {
        customViewLayout.findViewById<ConstraintLayout>(R.id.secondTextFieldLayout).visibility =
            View.VISIBLE

        customViewLayout.findViewById<TextInputLayout>(R.id.secondTextField).hint = "비밀번호"
    } // End of certifyViewSet

    private fun passwordEqualCheckObserver() {
        joinViewModel.confirmPassword.observe(viewLifecycleOwner) {
            if (joinViewModel.originPassword.value == joinViewModel.confirmPassword.value) {
                nextButton.isClickable = true
                nextButton.isEnabled = true
                secondEditTextMessageTv.text = ""
            } else {
                secondEditTextMessageTv.text = "비밀번호가 다릅니다."
                nextButton.isClickable = false
                nextButton.isEnabled = false
            }
        }
    } // End of passwordEqualCheckObserver
} // End of JoinPhoneNumberFragment class
