package com.app.myfoottrip.ui.view.splash

import android.content.Context
import android.os.Bundle
import android.view.View
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.FragmentSplashBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.start.StartActivity
import com.forms.sti.progresslitieigb.ProgressLoadingIGB

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
        startLoading()
    }

    private fun startLoading(){
        ProgressLoadingIGB.startLoadingIGB(requireContext()){
            message = "Good Morning!" //  Center Message
            srcLottieJson = R.raw.loading_splash // Tour Source JSON Lottie
            timer = 10000                   // Time of live for progress.
            hight = 500 // Optional
            width = 500 // Optional
        }
    }
}