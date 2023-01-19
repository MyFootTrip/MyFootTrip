package com.app.myfoottrip.ui.view.main

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.databinding.FragmentMyPageBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.util.GalleryUtils
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "MyPageFragment_마이풋트립"
class MyPageFragment : BaseFragment<FragmentMyPageBinding>(
    FragmentMyPageBinding::bind, R.layout.fragment_my_page
) {

    private var imageList = ArrayList<Uri>()
    private val boardViewModel by activityViewModels<BoardViewModel>()

    //-----
    private val imageLauncher = registerForActivityResult( //갤러리에서 선택하면 불러올 launcher
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "data: ${result} ")
            val imageUri: Uri? = result.data?.data //가져온 이미지의 uri
            Log.d(TAG, "uri : $imageUri")
            if (imageUri != null) { //이미지 가져오기 성공
                imageList.add(imageUri)
//                imageAdapter.apply { //recyclerview에 추가
//                    ImageList.add(imageUri)
//                    notifyItemInserted(ImageList.size - 1)
//                    Log.d(TAG, "images : ${ImageList.size}")
//                    binding.rvImages.visibility = View.VISIBLE
//                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            loadBtn.setOnClickListener {
                GalleryUtils.getGallery(requireContext(),imageLauncher)
            }

            saveBtn.setOnClickListener {
                val board = Board(9999,1,"테스트계정","string", Date(System.currentTimeMillis()),"혼자놀기","임시제목입니다"
                    ,"임시 내용입니다.", arrayListOf(), null,2,2)

                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO){
                        val urlList : ArrayList<String> = imageList.mapIndexed{ i, it ->
                            "IMAGE_${board.boardId}_${i}.png"
                        } as ArrayList<String>
                        board.imageList = GalleryUtils.insertImage(urlList,imageList,0)
                        Log.d(TAG, "이미지 저장 성공")
                    }
                    createBoard(board)

                    CoroutineScope(Dispatchers.Main).launch {
                        createBoardObserver()
                    }
                }
            }
        }
    }

    // ----------------Retrofit------------------
    //게시물 전체 받아오기
    private fun createBoard(board : Board){
        CoroutineScope(Dispatchers.IO).launch {
            boardViewModel.createBoard(board)
        }
    }

    private fun createBoardObserver() {
        boardViewModel.isCreated.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "createBoardObserver: ${it.data}")
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "게시물 조회 Error: ${it.data}")
                }
                is NetworkResult.Loading -> {

                }
            }
        }
    } // 게시물 전체 받아오기
}