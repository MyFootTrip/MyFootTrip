package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.app.myfoottrip.R

private const val TAG = "JoinBackButtonCustomVie_싸피"

class JoinBackButtonCustomView @JvmOverloads constructor(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {

    init {
        val inflater: LayoutInflater =
            getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.backbutton_custom_view, this, false)
        addView(view)
    }

}