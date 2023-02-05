package com.app.myfoottrip.ui.view.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.Application
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Email
import com.app.myfoottrip.data.viewmodel.FcmViewModel
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentLoginBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "LoginFragment_싸피"

class LoginFragment : BaseFragment<FragmentLoginBinding>(
    FragmentLoginBinding::bind, R.layout.fragment_login
) {
    private val userViewModel by activityViewModels<UserViewModel>()
    private val fcmViewModel by activityViewModels<FcmViewModel>()

    private lateinit var callback: OnBackPressedCallback
    private val navigationViewModel by activityViewModels<NavigationViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigationViewModel.startPage = 1
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            //뒤로가기
            btnBack.setOnClickListener {
                navigationViewModel.startPage = 1
                findNavController().popBackStack()
            }
        }

        binding.btnLogin.setOnClickListener {
            // 로그인을 할 수 있는 환경이 되면 로그인을 진행하도록 함
            if (checkLoginValid()) {
                login()
            }

        }

        userLoginResponseObserve()
    } // End of onViewCreated

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    } // End of onDetach

    private fun checkLoginValid(): Boolean {
        if (binding.etEmail.text!!.isEmpty()) {
            showToast("아이디를 입력해주세요")
            return false
        }

        if (binding.etPassword.text!!.isEmpty()) {
            showToast("비밀번호를 입력해주세요")
            return false
        }

        return true
    } // End of checkLoginValid

    private fun login() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        CoroutineScope(Dispatchers.IO).launch {
            userViewModel.userLogin(email, password)
        }
    } // End of login

    private fun userLoginResponseObserve() {
        userViewModel.userLoginResponseLiveData.observe(viewLifecycleOwner) {
            binding.loginProgressbar.isVisible = false
            binding.loginProgressbar.visibility = View.GONE

            when (it) {
                is NetworkResult.Success -> {
                    if (it.data!!.refresh_token != null && it.data!!.access_token != null) {
                        Log.d(TAG, "로그인 성공 ")

                        Application.sharedPreferencesUtil.addUserRefreshToken(it.data!!.refresh_token.toString())
                        Application.sharedPreferencesUtil.addUserAccessToken(it.data!!.access_token.toString())

                        addFcmTokenObserver()
                        addFcmToken()
                    }
                }

                is NetworkResult.Error -> {
                    showToast("아이디 또는 비밀번호가 잘못되었습니다. 다시 시도해주세요")
                    Log.d(TAG, "userLoginResponseObserve: ${it.data}")
                }

                is NetworkResult.Loading -> {
                    binding.loginProgressbar.isVisible = true
                    binding.loginProgressbar.visibility = View.VISIBLE
                    Log.d(TAG, "userLoginResponseObserve: 로딩 중")
                }
            }

        }
    } // End of userLoginResponseObserve

    //FCM 토큰 저장하기
    private fun addFcmToken() {
        CoroutineScope(Dispatchers.IO).launch {
            fcmViewModel.addFcmToken(Application.sharedPreferencesUtil.getFcmToken())
        }
    }

    private fun addFcmTokenObserver() {
        fcmViewModel.addFcmToken.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    val intent = Intent(activity, MainActivity::class.java) //fragment라서 activity intent와는 다른 방식
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intent)
                    activity!!.finish()
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }

} // End of LoginFragment class
