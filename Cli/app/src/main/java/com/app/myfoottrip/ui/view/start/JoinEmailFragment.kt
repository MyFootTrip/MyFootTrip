package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Email
import com.app.myfoottrip.data.viewmodel.JoinViewModel
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.databinding.FragmentJoinEmailBinding
import com.app.myfoottrip.util.NetworkResult
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "JoinEmailFragment_싸피"

class JoinEmailFragment : Fragment() {
    private lateinit var mContext: Context
    private lateinit var binding: FragmentJoinEmailBinding
    private lateinit var customViewLayout: JoinCustomView
    private lateinit var joinBackButtonCustomView: JoinBackButtonCustomView
    private val joinViewModel by activityViewModels<JoinViewModel>()

    private lateinit var emailWarningTextView: TextView
    private lateinit var secondTextFieldLayout: ConstraintLayout
    private lateinit var confirmNumberEditText: TextInputEditText
    private lateinit var emailTextFieldLayout: ConstraintLayout
    private lateinit var firstEditTextMessageTv: TextView
    private lateinit var nextButton: AppCompatButton
    private lateinit var inputEmailText: TextInputEditText
    private lateinit var emailValidBtn: AppCompatButton
    private lateinit var progressbar: ProgressBar
    private lateinit var emailConfirmButton: AppCompatButton



    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentJoinEmailBinding.inflate(inflater, container, false)
        return binding.root
    } // End of onCrateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customViewLayout = binding.joinCustomViewLayout
        joinBackButtonCustomView = customViewLayout.findViewById(R.id.join_back_button_customview)
        customViewDataInit()

        joinBackButtonCustomView.findViewById<AppCompatButton>(R.id.custom_back_button_appcompatbutton)
            .setOnClickListener {
                findNavController().popBackStack()
            }


        // 이메일 입력창의 값이 변해서 이메일 형식에 부합할 경우, 인증하기 버튼 상태 변화
        customViewLayout.findViewById<EditText>(R.id.editTextJoinEmail).addTextChangedListener {
            // 값이 변할 때 마다 이메일의 형식에 부합하는지를 확인

            if (emailTypeCheck() == true) {
                emailValidBtn.setTextColor(R.color.black)
                emailValidBtn.isClickable = true
                emailValidBtn.isEnabled = true
            }

            if (emailTypeCheck() == false) {
                emailValidBtn.setTextColor(R.color.join_confirm_button_basic_text_color)
                emailValidBtn.isClickable = false
                emailValidBtn.isEnabled = false
            }
        }

        // 이메일 인증 버튼 눌렀을 때,
        customViewLayout.findViewById<AppCompatButton>(R.id.emailConfirmButton).setOnClickListener {
            confirmNumberEditText.text!!.clear()
            emailValidBtn.isClickable = false
            emailValidBtn.isEnabled = false
            // 이메일 인증 버튼을 눌렀을 때, 이메일 사용여부 체크하는 통신 적용
            CoroutineScope(Dispatchers.IO).launch {
                joinViewModel.emailUsedCheck(Email(customViewLayout.findViewById<EditText>(R.id.editTextJoinEmail).text.toString()))
            }
        }

        // 다음 버튼 이벤트
        nextButton.setOnClickListener {
            joinViewModel.wholeJoinUserData.email =
                customViewLayout.findViewById<EditText>(R.id.editTextJoinEmail).text.toString()

            joinViewModel.wholeJoinUserData.password =
                customViewLayout.findViewById<EditText>(R.id.secondJoinEd).text.toString()

            Navigation.findNavController(customViewLayout.findViewById<AppCompatButton>(R.id.join_next_button))
                .navigate(R.id.action_emailJoinFragment_to_joinPhoneNumberFragment)
        }

        // 이메일 인증번호 입력창의 텍스트가 변할때마다 값을 비교해서 맞는지 아닌지 체크해줌
        confirmNumberEditText.addTextChangedListener {
            CoroutineScope(Dispatchers.IO).launch {
                joinViewModel.emailValidateCheck(
                    Email(
                        inputEmailText.text.toString(),
                        confirmNumberEditText.text.toString()
                    )
                )
            }
        }

        inputEmailText.addTextChangedListener {
            emailConfirmButton.text = "인증하기"
        }

        // 해당 이메일이 사용중인지 상태값을 가지고 있는 LiveData 옵저버
        isUsedEmailObserver()

        // 이메일 아이디 중복확인 & 인증 확인 모든 절차를 통과하는지 검사하는 LiveData 옵저버
        emailValidateCheckObserver()

    } // End of onViewCreated

    private fun customViewDataInit() {
        emailWarningTextView = customViewLayout.findViewById(R.id.firstEditTextMessageTv)
        secondTextFieldLayout = customViewLayout.findViewById(R.id.secondTextFieldLayout)
        emailConfirmButton = customViewLayout.findViewById(R.id.emailConfirmButton)

        // 다음 버튼
        nextButton = customViewLayout.findViewById(R.id.join_next_button)

        // 이메일 인증 버튼
        emailValidBtn = customViewLayout.findViewById(R.id.emailConfirmButton)

        // 인증번호 적는 EditText
        confirmNumberEditText = customViewLayout.findViewById(R.id.secondJoinEd)

        // 이메일 입력창 아래 줄
        emailTextFieldLayout = customViewLayout.findViewById(R.id.emailTextFieldLayout)

        firstEditTextMessageTv = customViewLayout.findViewById(R.id.firstEditTextMessageTv)

        // 이메일 적는 EditText
        inputEmailText = customViewLayout.findViewById(R.id.editTextJoinEmail)

        // 프로그레스 바
        progressbar = customViewLayout.findViewById(R.id.join_progressbar)
    } // End of customViewDataInit

    override fun onResume() {
        super.onResume()
        showViewInit()
    } // End of onResume

    // 다시 비워져야 할 항목들
    private fun showViewInit() {
        customViewLayout.findViewById<AppCompatButton>(R.id.emailConfirmButton).isClickable = false
        nextButton.isClickable = false
        nextButton.isEnabled = false
        progressbar.visibility = View.GONE
        secondTextFieldLayout.visibility = View.GONE
        confirmNumberEditText.text!!.clear()
        inputEmailText.text!!.clear()
        emailConfirmButton.text = "인증하기"
        emailWarningTextView.text = ""
    } // End of showViewInit

    private fun emailTypeCheck(): Boolean { // 이메일 형식 확인
        if (customViewLayout.findViewById<EditText>(R.id.editTextJoinEmail).text.isEmpty()) return false

        val text = customViewLayout.findViewById<EditText>(R.id.editTextJoinEmail).text
        return !TextUtils.isEmpty(text.toString()) && isEmailValid(text.toString())
    } // End of checkEmailType

    private fun isUsedEmailObserver() {
        joinViewModel.userResponseLiveData.observe(viewLifecycleOwner) {
            progressbar.isVisible = false
            progressbar.visibility = View.GONE
            firstEditTextMessageTv.setTextColor(R.color.black)

            emailValidBtn.setTextColor(R.color.join_confirm_button_basic_text_color)
            emailValidBtn.isClickable = false
            emailValidBtn.isEnabled = false

            when (it) {
                is NetworkResult.Success -> {
                    if (it.data == true) {
                        // 사용중인 이메일
                        secondTextFieldLayout.visibility = View.GONE
                        firstEditTextMessageTv.setTextColor(R.color.error_color)
                        emailWarningTextView.setText(R.string.isused_email_text)
                    }

                    if (it.data == false) {
                        // 사용가능한 이메일
                        secondTextFieldLayout.visibility = View.VISIBLE
                        emailWarningTextView.setText(R.string.can_use_email_text)
                    }
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "이메일 체크 Error: ${it.data}")
                }
                is NetworkResult.Loading -> {
                    progressbar.isVisible = true
                    progressbar.visibility = View.VISIBLE
                }
            }
        }

    } // End of isUsedEmailObserver

    private fun emailValidateCheckObserver() {
        joinViewModel.emailValidateResponse.observe(viewLifecycleOwner) {
            nextButton.isClickable = false
            nextButton.isEnabled = false
            progressbar.isVisible = false
            progressbar.visibility = View.GONE

            emailValidBtn.setTextColor(R.color.join_confirm_button_basic_text_color)
            emailValidBtn.isClickable = false
            emailValidBtn.isEnabled = false

            when (it) {
                is NetworkResult.Success -> {
                    if (it.data == true) {
                        emailWarningTextView.text = ("인증 성공")
                        nextButton.isClickable = true
                        nextButton.isEnabled = true
                    }

                    if (it.data == false) {
                        emailWarningTextView.text = ("인증 실패")

                        // 인증 실패할 경우, 재인증 버튼으로 바뀌면서 인증번호 EditText 전부 텍스트 없어짐
                        if (confirmNumberEditText.text!!.length == 6) {
                            confirmNumberEditText.text!!.clear()
                        }

                        emailConfirmButton.text = "재인증"
                    }
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "이메일 체크 Error: ${it.data}")
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "emailValidateCheckObserver: 로딩 중")
                    progressbar.isVisible = true
                    progressbar.visibility = View.VISIBLE
                }
            }
        }
    } // End of emailValidateCheckObserver

    private companion object {
        @JvmStatic
        val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        fun isEmailValid(email: String): Boolean {
            return EMAIL_REGEX.toRegex().matches(email);
        }
    } // End of companion object
} // End of JoinEmailFragment
