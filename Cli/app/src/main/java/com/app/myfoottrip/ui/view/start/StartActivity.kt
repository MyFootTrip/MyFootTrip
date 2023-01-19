package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.viewModels
import androidx.navigation.Navigation
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.StartViewModel
import com.app.myfoottrip.ui.adapter.ServiceClauseDetailAdapter
import com.app.myfoottrip.ui.view.dialogs.ServiceClauseCustomDialog
import com.app.myfoottrip.util.SharedPreferencesUtil

class StartActivity : AppCompatActivity() {
    private lateinit var dialog: ServiceClauseCustomDialog
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesUtil: SharedPreferencesUtil
    private val startViewModel: StartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // SharedPreference에 토큰이 있는지 없는지를 확인해야됨.
        sharedPreferencesUtil = SharedPreferencesUtil(this)
        val userToken = sharedPreferencesUtil.getUserToken()

        // 토큰이 있을 경우 해당 refreshToken을 기준으로 유효성 체크를 바로함
        if (userToken.refresh_token != "") {




        }


    } // End of onCreate

    fun showServiceDialog() {
        dialog = ServiceClauseCustomDialog()
        dialog.show(supportFragmentManager, "serviceClauseCustomDialog")
    } // End of showDialog
} // End of StartActivity class