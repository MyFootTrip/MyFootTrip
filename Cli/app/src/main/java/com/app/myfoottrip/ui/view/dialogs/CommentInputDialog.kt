package com.app.myfoottrip.ui.view.dialogs

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.User
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rengwuxian.materialedittext.MaterialEditText

class CommentInputDialog(private val listener: OnClickListener,private val user: User) :
    BottomSheetDialogFragment(),
    View.OnClickListener {

    lateinit var sendBtn: ImageView
    lateinit var commentMsg: MaterialEditText
    lateinit var profileImg : ImageView
    lateinit var profileBg : CardView

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

        //뷰 연결
        sendBtn = view.findViewById(R.id.iv_send)!!
        commentMsg = view.findViewById(R.id.et_comment)
        profileImg = view.findViewById(R.id.iv_profile)
        profileBg = view.findViewById(R.id.cv_profileLayout)
        sendBtn.setOnClickListener(this)

        //프로필 이미지
        if (user.join.profile_image.isNullOrEmpty()){
            profileImg.setPadding(20)
            Glide.with(this).asBitmap().load(R.drawable.ic_my).fitCenter().into(profileImg)
            profileImg.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white))
            profileBg.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.main)))
        }else {
            Glide.with(this).load(user.join.profile_image).centerCrop().into(profileImg)
            profileBg.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white)))
        }

        return view
    }

    interface OnClickListener {
        fun onClick(dialog: CommentInputDialog)
    }

    override fun onClick(p0: View?) {
        listener.onClick(this)
    }
}