package com.app.myfoottrip.ui.view.board

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.databinding.FragmentCreateBoardBinding
import com.app.myfoottrip.ui.adapter.HomeAdapter
import com.app.myfoottrip.ui.adapter.PhotoAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.GalleryUtils
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "CreateBoardFragment_마이풋트립"
class CreateBoardFragment : BaseFragment<FragmentCreateBoardBinding>(
    FragmentCreateBoardBinding::bind, R.layout.fragment_create_board
) {
    private lateinit var mainActivity: MainActivity

    private lateinit var photoAdapter: PhotoAdapter
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
                photoAdapter.apply { //recyclerview에 추가
                    notifyItemInserted(imageList.size - 1)
                    Log.d(TAG, "images : ${imageList.size}")
                }
            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() } //뒤로가기
            photoAddBtn.setOnClickListener { GalleryUtils.getGallery(requireContext(),imageLauncher)}
        }
    }

    private fun init(){
        initPhotoAdapter()
    }

    private fun initPhotoAdapter(){
        photoAdapter = PhotoAdapter(imageList)

        photoAdapter.setItemClickListener(object : PhotoAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int) {
                if (position == 0){

                }
            }
        })

        binding.rvPhoto.apply {
            adapter = photoAdapter
            //원래의 목록위치로 돌아오게함
            photoAdapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
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