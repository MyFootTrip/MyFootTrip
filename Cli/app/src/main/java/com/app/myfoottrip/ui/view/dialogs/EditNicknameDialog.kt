package com.app.myfoottrip.ui.view.dialogs;

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.rengwuxian.materialedittext.MaterialEditText

private const val TAG = "EditNicknameDialog_마이풋트립"
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
        Log.d(TAG, "다이얼로그 유저 데이터:${userViewModel.wholeMyData.value?.join} ")
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