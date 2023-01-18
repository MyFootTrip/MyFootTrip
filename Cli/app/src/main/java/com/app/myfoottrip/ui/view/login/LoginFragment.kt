package com.app.myfoottrip.ui.view.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Join
import com.app.myfoottrip.databinding.FragmentLoginBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.RetrofitUtil
import kotlinx.coroutines.launch

private const val TAG = "LoginFragment_마이풋트립"

class LoginFragment : BaseFragment<FragmentLoginBinding>(
    FragmentLoginBinding::bind, R.layout.fragment_login
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            //뒤로가기
            btnBack.setOnClickListener {
                findNavController().popBackStack()
            }

            btnLogin.setOnClickListener {
                login(
                    binding.etEmail.text.toString().trim(),
                    binding.etPassword.text.toString().trim()
                )
            }
        }
    }

    private fun login(email: String, password: String) {
        val user = Join(email, password)


        val intent = Intent(activity,MainActivity::class.java) //fragment라서 activity intent와는 다른 방식
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
//        lifecycleScope.launch {
//            try {
//                val api = RetrofitUtil.userService.login(user)
//                Log.d(TAG, "성공 login: $api")
//
//                val intent = Intent(activity,MainActivity::class.java) //fragment라서 activity intent와는 다른 방식
//                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
//                startActivity(intent)
//
//            }catch (e : Exception){
//                Log.e(TAG, "실패 login: $e " )
//            }
//        }
        lifecycleScope.launch {
            try {
                val api = RetrofitUtil.userService.login(user)
                Log.d(TAG, "성공 login: $api")
            } catch (e: Exception) {
                Log.e(TAG, "실패 login: $e ")
            }
        }
    }


}