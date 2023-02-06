package com.app.myfoottrip.ui.view.dialogs;

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.User
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.DialogEditNicknameBinding
import com.app.myfoottrip.ui.view.start.StartActivity
import com.app.myfoottrip.util.DeviceSizeUtil
import com.rengwuxian.materialedittext.MaterialEditText

class EditNicknameDialog(private val listener: OnClickListener) :
    DialogFragment(),
    View.OnClickListener {

    lateinit var btnSave: AppCompatButton
    lateinit var etEditNickname: MaterialEditText
    lateinit var ivCancelBtn: ImageView

    private val userViewModel by activityViewModels<UserViewModel>()

    val mTag = "닉네임 변경 다이얼로그"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view: View = inflater.inflate(R.layout.dialog_edit_nickname, container, false)

        // 뷰 연결
        btnSave = view.findViewById(R.id.btn_save)!!
        etEditNickname = view.findViewById(R.id.et_edit_nickname)
        ivCancelBtn = view.findViewById(R.id.iv_cancel_btn)

        ivCancelBtn.setOnClickListener{ dismiss() }
        btnSave.setOnClickListener(this)

        // 현재 유저 닉네임 가져오기
        etEditNickname.setText("${userViewModel.wholeMyData.value?.join?.nickname}")

        return view
    } // End of onCreateView

    interface OnClickListener {
        fun onClick(dialog: EditNicknameDialog)
    }

    override fun onClick(p0: View?) {
        listener.onClick(this)
    }

} // End of EditNicknameDialog
