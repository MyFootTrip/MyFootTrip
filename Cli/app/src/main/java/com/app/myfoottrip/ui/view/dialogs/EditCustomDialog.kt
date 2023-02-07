package com.app.myfoottrip.ui.view.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.app.myfoottrip.databinding.EditCustomDialogBinding

class EditCustomDialog(val text : String) :
    DialogFragment() {

    private lateinit var mContext: Context
    private var _binding: EditCustomDialogBinding? = null
    private val binding get() = _binding!!


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = EditCustomDialogBinding.inflate(inflater, container, false)

        _binding!!.finishButton.setOnClickListener {
            listener.onFinishClicked(this)
        }

        _binding!!.cancelButton.setOnClickListener {
            listener.onCancelClicked(this)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window!!.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setCancelable(true)
        dialog?.show()

        _binding!!.informTv.text = text
    }


    private lateinit var listener: ItemClickListener
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        listener = itemClickListener
    }

    interface ItemClickListener {
        fun onFinishClicked(dialog: DialogFragment)
        fun onCancelClicked(dialog: DialogFragment)
    } // End of OnDialogClickListener

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    } // End of onDestroyView
}
