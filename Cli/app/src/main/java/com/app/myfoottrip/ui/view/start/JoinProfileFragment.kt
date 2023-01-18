package com.app.myfoottrip.ui.view.start

import android.app.Activity
import android.app.Activity.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.app.myfoottrip.R
import com.app.myfoottrip.data.model.viewmodel.JoinViewModel
import com.app.myfoottrip.data.repository.UserRepository
import com.app.myfoottrip.databinding.FragmentJoinProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.parse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

private const val TAG = "JoinProfileFragment_싸피"

class JoinProfileFragment : Fragment() {
    private lateinit var mContext: Context
    private lateinit var binding: FragmentJoinProfileBinding
    private val joinViewModel by activityViewModels<JoinViewModel>()
    //private var imageUri: Uri? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    } // End of onCreate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinProfileBinding.inflate(inflater, container, false)
        return binding.root
    } // End of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.joinProfileImagePlusButton.setOnClickListener {
            Toast.makeText(mContext, "테스트 입니다.", Toast.LENGTH_SHORT).show()
        }

        binding.joinProfileImagePlusButton.setOnClickListener {
            getImage()
        }

        // 다음 버튼 클릭 이벤트
        binding.joinNextButton.setOnClickListener {
//            val file = File(absolutelyPath(imageUri, mContext))
//            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
//            val body = MultipartBody.Part.createFormData(
//                "profileImg", file.name, requestFile
//            )
            selectGallery()
        }

    } // End of onViewCreated

    private fun selectGallery() {
        val writePermission = ContextCompat.checkSelfPermission(
            mContext,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val readPermission = ContextCompat.checkSelfPermission(
            mContext,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (writePermission == PackageManager.PERMISSION_DENIED ||
            readPermission == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                mContext as Activity,
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQ_GALLERY
            )
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )
            imageResult.launch(intent)
        }
    }

    private val imageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data ?: return@registerForActivityResult

            val file = File(absolutelyPath(imageUri, mContext))
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            val body = MultipartBody.Part.createFormData("profileImg", file.name, requestFile)

            Log.d(TAG, "test: ${file.name}")

            CoroutineScope(Dispatchers.Main).launch {
                UserRepository().joinUser(body)
            }

        }
    }

    // 절대경로 변환
    private fun absolutelyPath(path: Uri?, context: Context): String {
        var proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        var c: Cursor? = context.contentResolver.query(path!!, proj, null, null, null)
        var index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()

        var result = c?.getString(index!!)
        return result!!
    }

    private fun getImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE_CODE)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (resultCode != RESULT_OK) {
//            return
//        }
//
//        when (requestCode) {
//            IMAGE_CODE -> {
//                data ?: return
//                imageUri = data.data as Uri
//            }
//        }
//    }

    companion object {
        const val IMAGE_CODE = 101
        const val REQ_GALLERY = 1
    }

} // End of JoinProfileFragment class
