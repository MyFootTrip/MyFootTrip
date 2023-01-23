package com.app.myfoottrip.ui.view.start

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.RefreshToken
import com.app.myfoottrip.data.viewmodel.StartViewModel
import com.app.myfoottrip.ui.view.dialogs.ServiceClauseCustomDialog
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.SharedPreferencesUtil
import com.app.myfoottrip.util.showToastMessage


private const val TAG = "StartActivity_싸피"

class StartActivity : AppCompatActivity() {
    private lateinit var dialog: ServiceClauseCustomDialog
    private lateinit var sharedPreferencesUtil: SharedPreferencesUtil
    private val startViewModel: StartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // SharedPreference에 토큰이 있는지 없는지를 확인해야됨.
        sharedPreferencesUtil = SharedPreferencesUtil(this)
        val refreshToken = sharedPreferencesUtil.getUserRefreshToken()

        Log.d(TAG, "refreshToken : $refreshToken")

        // 토큰이 있을 경우 해당 refreshToken을 기준으로 유효성 체크를 실시해서
        // 토큰의 기한이 맞을 경우 accessToken을 발급 받을 수 있는지를 확인함
        if (refreshToken != "") {
            startViewModel.refreshTokenValidCheck(RefreshToken(refreshToken))
        }

        refreshTokenValidCheckObserver()
    } // End of onCreate

    fun showServiceDialog() {
        dialog = ServiceClauseCustomDialog()
        dialog.show(supportFragmentManager, "serviceClauseCustomDialog")
    } // End of showDialog

    private fun refreshTokenValidCheckObserver() {
        startViewModel.refreshTokenValidCheckResponseLiveData.observe(this) {
            // refreshToken이 정상적으로 확인되면
            // accessToKen을 가지고 곧바로 MainActivity로 넘어감

            when (it) {
                is NetworkResult.Success -> {
                    if (it.data!!.access.toString() == "") {
                        this.showToastMessage("빈 값이 넘어옴")
                    }

                    if (it.data!!.access.toString() != "") {
                        this.showToastMessage("발급된 토큰 : ${it.data!!.access.toString()}")
                    }
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "refreshToken Error : ${it.data},  ${it.message} ")
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "refreshToken 로딩중 ")
                }
            }
        }

    } // End of refreshTokenValidCheckObserver

} // End of StartActivity class
