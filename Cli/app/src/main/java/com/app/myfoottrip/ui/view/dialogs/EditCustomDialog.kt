package com.app.myfoottrip.ui.view.dialogs

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.databinding.EditCustomDialogBinding
import com.app.myfoottrip.ui.view.travel.PlaceImageAdapter
import com.app.myfoottrip.util.DeviceSizeUtil

class EditCustomDialog(val placeData: Place) :
    DialogFragment() {

    private lateinit var mContext: Context
    private var _binding: EditCustomDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var size: Point
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeImageAdapter: PlaceImageAdapter


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
        size = DeviceSizeUtil.deviceSizeCheck(mContext)


        _binding!!.finishButton.setOnClickListener {
            listener.onFinishClicked(this)
        }

        _binding!!.cancelButton.setOnClickListener {
            listener.onCancelClicked(this)
        }

        _binding!!.imageAddButton.setOnClickListener {
            listener.onImageAddButtonClicked(this)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = (size.x * 0.87).toInt()
        params?.height = (size.y * 0.77).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setCancelable(true)
        dialog?.show()
        // _binding!!.informTv.text = text

        binding.addressEd.setText(placeData.address.toString())
        binding.contentEd.setText(placeData.memo.toString())
        binding.locationNameTv.text = placeData.placeName.toString()

        // 리사이클러뷰 바인딩
        recyclerView = _binding!!.placeEditRecyclerview
        recyclerView.apply {
            adapter = placeImageAdapter
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        }
    } // End of onViewCreated

    private lateinit var listener: ItemClickListener
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        listener = itemClickListener
    }

    interface ItemClickListener {
        fun onFinishClicked(dialog: DialogFragment)
        fun onCancelClicked(dialog: DialogFragment)
        fun onImageAddButtonClicked(dialog: DialogFragment)
    } // End of OnDialogClickListener

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    } // End of onDestroyView
} // End of EditCustomDialog class
