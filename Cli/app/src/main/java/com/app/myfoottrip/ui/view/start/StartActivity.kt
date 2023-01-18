package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import com.app.myfoottrip.R
import com.app.myfoottrip.ui.adapter.ServiceClauseDetailAdapter
import com.app.myfoottrip.ui.view.dialogs.ServiceClauseCustomDialog

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