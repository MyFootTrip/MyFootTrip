package com.app.myfoottrip.ui.view.dialogs;

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.databinding.DialogEditNicknameBinding
import com.app.myfoottrip.util.DeviceSizeUtil

class EditNicknameDialog() : DialogFragment() {
    private var _binding: DialogEditNicknameBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var size: Point

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogEditNicknameBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        size = DeviceSizeUtil.deviceSizeCheck(mContext)

        return binding.root
    } // End of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivCancelBtn.setOnClickListener { dismiss() } // 취소 버튼 클릭
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    } // End of onDestroyView

} // End of EditNicknameDialog
