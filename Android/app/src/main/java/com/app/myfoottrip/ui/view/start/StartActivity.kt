package com.app.myfoottrip.ui.view.start

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.app.myfoottrip.R
import com.app.myfoottrip.viewmodel.NavigationViewModel
import com.app.myfoottrip.databinding.ActivityStartBinding
import com.app.myfoottrip.ui.view.dialogs.ServiceClauseCustomDialog


private const val TAG = "StartActivity_마이풋트립"

class StartActivity : AppCompatActivity() {
    private lateinit var dialog: ServiceClauseCustomDialog

    private val navigationViewModel by viewModels<NavigationViewModel>()
    private var page: Int = 0

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.INTERNET
            ), 0
        )

        page = intent.getIntExtra("page", 0)
        if (page == 1) navigationViewModel.startPage = 1

        setBinding()

    } // End of onCreate

    private fun setBinding() {
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
    } // End of setBinding

} // End of StartActivity class
