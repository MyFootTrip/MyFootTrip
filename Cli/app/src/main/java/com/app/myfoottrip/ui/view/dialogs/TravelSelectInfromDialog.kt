package com.app.myfoottrip.ui.view.dialogs

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.app.myfoottrip.databinding.EditCustomDialogBinding
import com.app.myfoottrip.databinding.TravelSelectInformDialogBinding
import com.app.myfoottrip.util.DeviceSizeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class TravelSelectInfromDialog(val informText: String) : DialogFragment() {
    private lateinit var mContext: Context
    private var _binding: TravelSelectInformDialogBinding? = null
    private val binding get() = _binding!!
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
        _binding = TravelSelectInformDialogBinding.inflate(inflater, container, false)
        size = DeviceSizeUtil.deviceSizeCheck(mContext)
        return binding.root
    } // End of onCreateView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = (size.x * 0.47).toInt()
        params?.height = (size.y * 0.47).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setCancelable(true)
        dialog?.show()

        _binding!!.informTv.text = informText

        _binding!!.cancelButton.setOnClickListener {
            dialog!!.dismiss()
        }

        _binding!!.submitButton.setOnClickListener {
            listener.onSubmitButtonClicked()
        }
    } // End of onViewCreated

    private lateinit var listener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        listener = itemClickListener
    } // End of setItemClickListener

    interface ItemClickListener {
        fun onSubmitButtonClicked()
    } // End of OnDialogClickListener

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    } // End of onDestroyView
} // End of TravelSelectInfromDialog class
