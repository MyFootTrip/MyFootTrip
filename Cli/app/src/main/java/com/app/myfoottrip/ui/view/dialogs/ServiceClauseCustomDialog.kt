package com.app.myfoottrip.ui.view.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.databinding.DialogServiceClauseBinding
import com.app.myfoottrip.ui.adapter.ServiceClauseDetailAdapter
import com.app.myfoottrip.ui.view.start.StartActivity
import com.app.myfoottrip.util.DeviceSizeUtil
import com.app.myfoottrip.util.allServiceClauseList

class ServiceClauseCustomDialog() : DialogFragment() {
    private var _binding: DialogServiceClauseBinding? = null
    private val binding get() = _binding!!
    private lateinit var serviceClauseDetailAdapter: ServiceClauseDetailAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mContext: Context
    private lateinit var size: Point

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context as StartActivity
    } // End of onAttach

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogServiceClauseBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        size = DeviceSizeUtil.deviceSizeCheck(mContext)

        return binding.root
    } // End of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.serviceClauseDialogRecyclerview
        initDialog()

        // 다이얼로그 레이아웃매니저
        recyclerView.apply {
            adapter = serviceClauseDetailAdapter
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        }
    } // End of onViewCreated

    override fun onResume() {
        super.onResume()

        // device 사이즈를 체크해서 dialogFragment가 항상 사용자의 디바이스 너비의 90%와 높이는 컨텐츠의 크기에 따라 자동 조절되도록 설정.
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = (size.x * 0.87).toInt()
        params?.height = (size.y * 0.77).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    } // End of onResume

    private fun initDialog() {
        serviceClauseDetailAdapter = ServiceClauseDetailAdapter(mContext, allServiceClauseList)
    } // End of initData

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    } // End of onDestroyView
} // End of ServiceClauseCustomDialog
