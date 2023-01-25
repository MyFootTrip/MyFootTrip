package com.app.myfoottrip.ui.view.splash

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.FragmentSplashBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.start.StartActivity
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.forms.sti.progresslitieigb.ProgressLoadingIGB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : BaseFragment<FragmentSplashBinding>(
    FragmentSplashBinding::bind, R.layout.fragment_splash
) {
    private lateinit var startActivity: StartActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        startActivity = context as StartActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init(){
        lottieControl()
    }

    //로띠 애니메이션
    private fun lottieControl(){
        binding.apply {

            lottieView.speed = 1f
            lottieView.visibility = View.VISIBLE
            lottieView.playAnimation()

            lottieView.addAnimatorListener(object : Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator?) {
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

                        showLottieProgress()
                    }

                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }

            })
        }
    }

    //프로그레스바 Lottie
    private suspend fun showLottieProgress(){
        //프로그레스 애니메이션 시작
        binding.apply {
            delay(1000)
            lottieProgress.visibility = View.VISIBLE
            lottieProgress.playAnimation()
            login()
        }
    }

    //로그인 창으로 이동
    private suspend fun login(){
        delay(2000)
        findNavController().navigate(R.id.action_splashFragment_to_startFragment)
    }
}