package com.app.myfoottrip.ui.view.dialogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.data.dao.VisitPlaceRepository
import com.app.myfoottrip.data.dto.VisitPlace
import com.app.myfoottrip.data.viewmodel.EditSaveViewModel
import com.app.myfoottrip.databinding.EditCustomDialogBinding
import com.app.myfoottrip.ui.view.travel.EditSaveTravelFragment
import com.app.myfoottrip.ui.view.travel.PlaceImageAdapter
import com.app.myfoottrip.util.DeviceSizeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

private const val TAG = "EditCustomDialog_싸피"

class EditCustomDialog(var placeData: VisitPlace) : DialogFragment() {

    // ViewModel
    private val editSaveViewModel by activityViewModels<EditSaveViewModel>()

    private lateinit var mContext: Context
    private var _binding: EditCustomDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var size: Point
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeImageAdapter: PlaceImageAdapter

    // RoomDB
    lateinit var visitPlaceRepository: VisitPlaceRepository

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        visitPlaceRepository = VisitPlaceRepository.get()
    } // End of onCreate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = EditCustomDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        size = DeviceSizeUtil.deviceSizeCheck(mContext)

        _binding!!.deleteButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                listener.onDeleteClicked()
            }
        }

        _binding!!.addressEd.setText(placeData.address)
        _binding!!.contentEd.setText(placeData.content.toString())
        _binding!!.locationNameEd.setText(placeData.placeName.toString())

        // 저장 버튼을 눌렀을 때,
        _binding!!.saveButton.setOnClickListener {
            placeData.content = _binding!!.contentEd.text.toString()
            placeData.address = _binding!!.addressEd.text.toString()
            placeData.placeName = (_binding!!.locationNameEd.text.toString())

            CoroutineScope(Dispatchers.IO).launch {
                visitPlaceRepository.updateVisitPlace(placeData)
                withContext(Dispatchers.Main) {
                    val ed = EditSaveTravelFragment()
//                    ed.setAdapterRefresh()
                }
                dialog!!.dismiss()
            }
        }

        _binding!!.cancelButton.setOnClickListener {
            dialog!!.dismiss()
        }

        _binding!!.imageAddButton.setOnClickListener {
            selectGallery()
        }

        return binding.root
    } // End of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectUserImageListObserve()

        editSaveViewModel.clearDeleteImageList()

        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        params?.width = (size.x * 0.87).toInt()
        params?.height = (size.y * 0.77).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.setCancelable(true)
        dialog?.show()

        // 리사이클러뷰 바인딩
        recyclerView = _binding!!.placeEditRecyclerview
        val size = placeData.imgList.size
        val uriList: MutableList<Uri> = LinkedList()
        for (i in 0 until size) {
            val temp = placeData.imgList[i]

            // List를 Uri로 바꿔서 넣어주기.
            uriList.add(temp.toUri())
        }

        placeImageAdapter = PlaceImageAdapter(mContext, uriList)

        recyclerView.apply {
            adapter = placeImageAdapter
            layoutManager = LinearLayoutManager(mContext, GridLayoutManager.HORIZONTAL, false)
        }

        // 이미지 리사이클러뷰 이벤트 처리
        placeImageAdapter.setItemClickListener(object : PlaceImageAdapter.ItemClickListener {
            // 이미지 삭제 버튼 눌렀을 때,
            override fun onRemoveImageButtonClicked(position: Int) {

                // 기존에 있던 서버에서 들어온 이미지 일 경우 삭제되었다는 정보를 넘겨주어야 한다.
                val name = placeData.imgList[position].substring(0, 4)
                // 기존에 있던 이미지인지를 판별함
                if (name == "http") {
                    val split = placeData.imgList[position].split("media/")
                    editSaveViewModel.addDeleteImageList(split[1])
                }

                placeData.imgList.removeAt(position)
                placeImageAdapter.removeImgage(position)
            }
        })

    } // End of onViewCreated

    private lateinit var listener: ItemClickListener
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        listener = itemClickListener
    } // End of setItemClickListener

    interface ItemClickListener {
        fun onDeleteClicked()
        suspend fun onSaveClicked()
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
            editSaveViewModel.setSelectUserImageLiveData(imageUri)

        }
    } // End of registerForActivityResult

    private fun selectUserImageListObserve() {
        editSaveViewModel.selectUserImageList.observe(viewLifecycleOwner) {

            if (it != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    editSaveViewModel.clearSelectUserImageLiveData()
                    placeData.imgList.add(it.toString())
                    placeImageAdapter.addData(it)
                }
            }
        }
    } // End of selectUserImageListObserve

    interface RefreshListener {
        fun onRefresh()
    } // End of RefreshListener

    private lateinit var refreshListener : RefreshListener

    fun setRefreshListener(refreshListener : RefreshListener) {
        this.refreshListener = refreshListener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        refreshListener.onRefresh()
        _binding = null
    } // End of onDestroyView

    companion object {
        const val REQ_GALLERY = 1
    }
} // End of EditCustomDialog class
