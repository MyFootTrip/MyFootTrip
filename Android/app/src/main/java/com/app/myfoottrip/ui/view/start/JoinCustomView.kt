package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.myfoottrip.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

private const val TAG = "싸피"

class JoinCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
) : ConstraintLayout(context, attrs) {

    private var join_title: AppCompatTextView? = null
    private var password_confirm_layout: ConstraintLayout? = null
    private var email_input_layout: TextInputLayout? = null
    private var email_input_text: TextInputEditText? = null
    private var confirm_button: AppCompatButton? = null
    private var secondTextField: TextInputLayout? = null
    private var secondJoinEd: TextInputEditText? = null


    init {
        val inflater: LayoutInflater =
            getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.join_custom_layout, this, false)
        addView(view)

        join_title = view.findViewById(R.id.join_title)
        email_input_layout = view.findViewById(R.id.emailTextField)
        email_input_text = view.findViewById(R.id.editTextJoinEmail)
        confirm_button = view.findViewById(R.id.emailConfirmButton)
        secondTextField = view.findViewById(R.id.secondTextField)
        secondJoinEd = view.findViewById(R.id.secondJoinEd)
        password_confirm_layout = view.findViewById(R.id.secondTextFieldLayout)
    }

    fun setView(joinTypeNum: Int) {
        // 첫 번째 이메일로 회원가입
        if (joinTypeNum == 1) {
            join_title!!.setText(R.string.email_join_title)
            secondTextField!!.hint = "인증번호"
        }

        if (joinTypeNum == 2) {
            // 입력 최대 길이값.
            val maxLength = 20
            // 두 번째 비밀번호 설정 화면
            join_title!!.setText(R.string.password_join_title)

            // 비밀번호 확인 레이아웃은 보이지 않도록 설정
            email_input_layout!!.hint = "비밀번호"
            confirm_button!!.visibility = View.GONE
            email_input_text!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
            email_input_layout!!.isPasswordVisibilityToggleEnabled = true
            email_input_text!!.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD


            secondTextField!!.hint = "비밀번호 확인"
            password_confirm_layout!!.visibility = View.VISIBLE
            secondJoinEd!!.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            secondTextField!!.isPasswordVisibilityToggleEnabled = true
            secondJoinEd!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))


        }
    } // End of setText
} // End of JoinCustomView class
