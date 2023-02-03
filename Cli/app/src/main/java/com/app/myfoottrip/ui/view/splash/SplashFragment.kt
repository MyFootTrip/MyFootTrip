package com.app.myfoottrip.ui.view.splash

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.Application
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.data.viewmodel.FcmViewModel
import com.app.myfoottrip.data.viewmodel.StartViewModel
import com.app.myfoottrip.databinding.FragmentSplashBinding
import com.app.myfoottrip.network.fcm.MyFireBaseMessagingService
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.ui.view.start.StartActivity
import com.app.myfoottrip.ui.view.start.StartFragment
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.SharedPreferencesUtil
import com.app.myfoottrip.util.showToastMessage
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.coroutines.*


private const val TAG = "SplashFragment_마이풋트립"

class SplashFragment : BaseFragment<FragmentSplashBinding>(
    FragmentSplashBinding::bind, R.layout.fragment_splash
) {
    private lateinit var startActivity: StartActivity
    private val startViewModel by activityViewModels<StartViewModel>()
    private lateinit var sharedPreferencesUtil: SharedPreferencesUtil
    private val fcmViewModel by activityViewModels<FcmViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        startActivity = context as StartActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        lottieControl()
        
        getFcmToken()

        binding.lottieProgress.visibility = View.INVISIBLE
        binding.lottieProgress.playAnimation()

        refreshTokenValidCheckObserver()
    }
    
    //SharedPreference에 FCM 토큰 저장
    private fun getFcmToken(){
        MyFireBaseMessagingService().getFirebaseToken()
    }

    //로띠 애니메이션
    private fun lottieControl() {
        binding.apply {
            lottieView.speed = 1f
            lottieView.visibility = View.VISIBLE
            lottieView.playAnimation()

            lottieView.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    //
                }

                //애니메이션이 끝난 후
                override fun onAnimationEnd(animation: Animator?) {
                    lottieView.visibility = View.INVISIBLE
                    ivLogo.visibility = View.VISIBLE

                    CoroutineScope(Dispatchers.Main).launch {
                        YoYo.with(Techniques.FadeIn) //화면에 FadeIn 효과를 처리한 로고뷰가 나타남
                            .duration(700)
                            .repeat(1)
                            .playOn(ivLogoText)

                        ivLogoText.visibility = View.VISIBLE

                        delay(1000)
                        // SharedPreference에 토큰이 있는지 없는지를 확인해야됨.
                        binding.lottieProgress.visibility = View.VISIBLE
                        binding.lottieProgress.playAnimation()
                        sharedPreferencesUtil = SharedPreferencesUtil(startActivity)
                        val refreshToken = sharedPreferencesUtil.getUserRefreshToken()

                        // 토큰이 있을 경우 해당 refreshToken을 기준으로 유효성 체크를 실시해서
                        // 토큰의 기한이 맞을 경우 accessToken을 발급 받을 수 있는지를 확인함
                        if (refreshToken != "") {
                            startViewModel.refreshTokenValidCheck(Token("", refreshToken))
                        } else {
                            parentFragmentManager.beginTransaction().replace(R.id.fragment_start, StartFragment()).commit()
                        }
                    }

                } // End of onAnimationEnd

                override fun onAnimationCancel(animation: Animator?) {
                    //
                }

                override fun onAnimationRepeat(animation: Animator?) {
                    //
                }

            })
        }
    } // End of lottieControl

    private fun refreshTokenValidCheckObserver() {
        startViewModel.refreshTokenValidCheckResponseLiveData.observe(viewLifecycleOwner) {
            // refreshToken이 정상적으로 확인되면
            // accessToKen을 가지고 곧바로 MainActivity로 넘어감

            when (it) {
                is NetworkResult.Success -> {
                    if (it.data!!.access_token.toString() == "") {
                        startActivity.showToastMessage("빈 값이 넘어옴")
                    }

                    if (it.data!!.access_token.toString() != "") {
                        // 만약 발급된 accessToken이 있다면
                        // sharedPreference에 accesstoken을 저장
                        Application.sharedPreferencesUtil.addUserAccessToken(it.data!!.access_token.toString())
                        Application.sharedPreferencesUtil.addUserRefreshToken(it.data!!.refresh_token.toString())
                        addFcmTokenObserver()
                        addFcmToken()
                    }
                }
                is NetworkResult.Error -> {
                    if (it.data?.access_token == null) {
                        startActivity.showToastMessage("토큰이 만료되었습니다 로그인을 다시 해주세요")
                        parentFragmentManager.beginTransaction().replace(R.id.fragment_start, StartFragment()).commit()
                    }
                }
                is NetworkResult.Loading -> {
                    binding.lottieProgress.visibility = View.VISIBLE
                    binding.lottieProgress.playAnimation()
                    Log.d(TAG, "refreshToken 로딩중 ")
                }
            }
        }
    } // End of refreshTokenValidCheckObserver

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
                    val intent = Intent(activity, MainActivity::class.java)
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
}