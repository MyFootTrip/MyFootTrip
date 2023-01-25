package com.app.myfoottrip.ui.view.start

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.StartViewModel
import com.app.myfoottrip.ui.view.dialogs.ServiceClauseCustomDialog
import com.app.myfoottrip.util.SharedPreferencesUtil


private const val TAG = "StartActivity_싸피"

class StartActivity : AppCompatActivity() {
    private lateinit var dialog: ServiceClauseCustomDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

    } // End of onCreate

    fun showServiceDialog() {
        dialog = ServiceClauseCustomDialog()
        dialog.show(supportFragmentManager, "serviceClauseCustomDialog")
    } // End of showDialog
} // End of StartActivity class
