package com.app.myfoottrip.ui.view.dialogs

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import com.app.myfoottrip.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rengwuxian.materialedittext.MaterialEditText

class CommentInputDialog(private val listener: OnClickListener) :
    BottomSheetDialogFragment(),
    View.OnClickListener {

    lateinit var sendBtn: ImageView
    lateinit var comment : MaterialEditText
    val mTag = "댓글 입력 다이얼로그"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomInputDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val view: View = inflater.inflate(R.layout.dialog_comment_input, container, false)

        // 텍스트뷰 세팅
        sendBtn = view.findViewById(R.id.iv_send)!!
        comment = view.findViewById(R.id.et_comment)
        sendBtn.setOnClickListener(this)

        return view
    }

    interface OnClickListener {
        fun onClick(dialog: CommentInputDialog)
    }

    override fun onClick(p0: View?) {
        listener.onClick(this)
    }
}