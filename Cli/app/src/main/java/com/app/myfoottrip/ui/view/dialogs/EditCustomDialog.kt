package com.app.myfoottrip.ui.view.dialogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.data.dao.VisitPlaceRepository
import com.app.myfoottrip.data.dto.VisitPlace
import com.app.myfoottrip.data.viewmodel.EditSaveViewModel
import com.app.myfoottrip.databinding.EditCustomDialogBinding
import com.app.myfoottrip.ui.view.start.JoinProfileFragment.Companion.REQ_GALLERY
import com.app.myfoottrip.ui.view.travel.EditSaveTravelFragment
import com.app.myfoottrip.ui.view.travel.PlaceImageAdapter
import com.app.myfoottrip.util.DeviceSizeUtil
import com.kakao.sdk.template.model.Link
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedList

private const val TAG = "EditCustomDialog_싸피"

class EditCustomDialog(var placeData: VisitPlace) :
    DialogFragment() {

    // ViewModel
    private val editSaveViewModel by viewModels<EditSaveViewModel>()

    private lateinit var mContext: Context
    private var _binding: EditCustomDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var size: Point
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeImageAdapter: PlaceImageAdapter

    // RoomDB
    lateinit var visitPlaceRepository: VisitPlaceRepository

    // imageList
    val imageList : MutableList<Uri> = placeData.imgList

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitPlaceRepository = VisitPlaceRepository.get()
    }

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
            //


        }

        _binding!!.imageAddButton.setOnClickListener {
            selectGallery()
        }

        return binding.root
    } // End of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectUserImageListObserve()

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
        placeImageAdapter = PlaceImageAdapter(mContext)
        placeImageAdapter.setList(imageList)

        recyclerView.apply {
            adapter = placeImageAdapter
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        }
    } // End of onViewCreated

    private lateinit var listener: ItemClickListener
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        listener = itemClickListener
    } // End of setItemClickListener

    interface ItemClickListener {
        suspend fun onDeleteClicked()
        suspend fun onSaveClicked()
        //suspend fun onImageAddButtonClicked()
    } // End of OnDialogClickListener


    private fun selectGallery() {
        val writePermission = ContextCompat.checkSelfPermission(
            mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            mContext, android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (writePermission == PackageManager.PERMISSION_DENIED || readPermission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                mContext as Activity, arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), REQ_GALLERY
            )
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*"
            )
            imageResult.launch(intent)
        }
    } // End of selectGallery

    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        // 가져온 이미지가 있을 경우 해당 데이터를 불러옴.
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data ?: return@registerForActivityResult
            editSaveViewModel.setSelectUserImageList(imageUri)
        }
    } // End of registerForActivityResult

    private fun selectUserImageListObserve() {
        editSaveViewModel.selectUserImageList.observe(viewLifecycleOwner) {
            imageList.add(it)
            placeImageAdapter.addItem(it)
        }
    } // End of selectUserImageListObserve

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    } // End of onDestroyView

    companion object {
        const val REQ_GALLERY = 1
    }

} // End of EditCustomDialog class
