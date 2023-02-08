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
import com.app.myfoottrip.data.dto.VisitPlace
import com.app.myfoottrip.databinding.EditCustomDialogBinding
import com.app.myfoottrip.ui.view.travel.PlaceImageAdapter
import com.app.myfoottrip.util.DeviceSizeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditCustomDialog(val placeData: VisitPlace) :
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

        _binding!!.deleteButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                listener.onDeleteClicked()
            }
        }

        _binding!!.saveButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                listener.onSaveClicked()
            }
        }

        _binding!!.imageAddButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                listener.onImageAddButtonClicked()
            }
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

        binding.addressEd.setText(placeData.address)
        binding.contentEd.setText(placeData.content.toString())
        binding.locationNameTv.text = placeData.placeName.toString()

        // 리사이클러뷰 바인딩
        recyclerView = _binding!!.placeEditRecyclerview
        placeImageAdapter = PlaceImageAdapter(mContext, placeData.imgList)
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
        suspend fun onDeleteClicked()
        suspend fun onSaveClicked()
        suspend fun onImageAddButtonClicked()
    } // End of OnDialogClickListener

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    } // End of onDestroyView
} // End of EditCustomDialog class
