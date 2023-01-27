package com.app.myfoottrip.ui.view.board


import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.databinding.FragmentCreateBoardBinding
import com.app.myfoottrip.ui.adapter.PhotoAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.GalleryUtils
import com.app.myfoottrip.util.NetworkResult
import com.google.android.datatransport.runtime.firebase.transport.LogEventDropped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

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
            val imageUri: Uri? = result.data?.data //가져온 이미지의 uri
            if (imageUri != null) { //이미지 가져오기 성공
                imageList.add(imageUri)
                photoAdapter.apply { //recyclerview에 추가
                    notifyItemInserted(imageList.size - 1)
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
            photoAddBtn.setOnClickListener {
                GalleryUtils.getGallery(
                    requireContext(),
                    imageLauncher
                )
            } //갤러리 이미지 불러오기 버튼

            //게시물 등록하기 버튼
            btnCreate.setOnClickListener {
                val board = Board(1, 1, "테스트계정", "string", Date(System.currentTimeMillis()), "혼자놀기", "임시제목입니다", "임시 내용입니다.", arrayListOf(), Travel(1,
                    arrayListOf(), Date(System.currentTimeMillis()),
                    Date(System.currentTimeMillis()),
                    arrayListOf()), arrayListOf(), arrayListOf())
                imageList.removeAt(0)
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO) {
                        val urlList: ArrayList<String> = List(imageList.size) { i ->
                            "IMAGE_${board.boardId}_${i}.png"
                        } as ArrayList<String>
                        board.imageList = GalleryUtils.insertImage(urlList, imageList, 0)
                        board.content = etContent.text.toString()
                    }
                    createBoard(board)

                    CoroutineScope(Dispatchers.Main).launch {
                        createBoardObserver()
                    }
                }
            }
        }
    }

    private fun init() {
        val uri = Uri.parse("https://images.velog.io/images/ccmmss98/post/4de24da3-70a1-4a57-8df8-7d8bd8ef2b70/saffy.png")
        imageList.add(uri)
        initPhotoAdapter()
    }

    private fun initPhotoAdapter() {
        photoAdapter = PhotoAdapter(imageList)

        photoAdapter.setItemClickListener(object : PhotoAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int) {
                imageList.removeAt(position)
                photoAdapter.notifyItemRemoved(position)
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
    private fun createBoard(board: Board) {
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