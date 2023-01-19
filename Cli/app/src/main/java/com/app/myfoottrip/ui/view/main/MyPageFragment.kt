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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            loadBtn.setOnClickListener {

            }

//            saveBtn.setOnClickListener {
//                val board = Board(9999,1,"테스트계정","string", Date(System.currentTimeMillis()),"혼자놀기","임시제목입니다"
//                    ,"임시 내용입니다.", arrayListOf(), null,2,2)
//
//                CoroutineScope(Dispatchers.IO).launch {
//                    withContext(Dispatchers.IO){
//                        val urlList : ArrayList<String> = imageList.mapIndexed{ i, it ->
//                            "IMAGE_${board.boardId}_${i}.png"
//                        } as ArrayList<String>
//                        board.imageList = GalleryUtils.insertImage(urlList,imageList,0)
//                        Log.d(TAG, "이미지 저장 성공")
//                    }
//                    createBoard(board)
//
//                    CoroutineScope(Dispatchers.Main).launch {
//                        createBoardObserver()
//                    }
//                }
//            }
        }
    }
}