package com.app.myfoottrip.ui.view.board

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Board
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.databinding.FragmentUpdateBoardBinding
import com.app.myfoottrip.ui.adapter.PhotoAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.AlertDialog
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.GalleryUtils
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "UpdateBoardFragment_마이풋트립"
class UpdateBoardFragment : BaseFragment<FragmentUpdateBoardBinding>(
    FragmentUpdateBoardBinding::bind, R.layout.fragment_update_board
){
    private lateinit var mainActivity : MainActivity
    private val navigationViewModel by activityViewModels<NavigationViewModel>()
    private lateinit var callback: OnBackPressedCallback

    private val boardViewModel by activityViewModels<BoardViewModel>()
    private lateinit var photoAdapter: PhotoAdapter
    private var imageList = ArrayList<Uri>()

    private var insertIndex = 0
    private var isInsert = false

    private val imageLauncher = registerForActivityResult( //갤러리에서 선택하면 불러올 launcher
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data //가져온 이미지의 uri
            if (imageUri != null) { //이미지 가져오기 성공
                imageList.add(imageUri)
                insertIndex++
                Log.d(TAG, "이미지 삽입: $imageUri")
                photoAdapter.apply { //recyclerview에 추가
                    notifyItemInserted(imageList.size - 1)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        binding.apply {
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            photoAddBtn.setOnClickListener { //갤러리 이미지 불러오기 버튼
                GalleryUtils.getGallery(requireContext(), imageLauncher)
            }
            btnUpdate.setOnClickListener {
                showDialog()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun init(){
        val uri = Uri.parse("https://images.velog.io/images/ccmmss98/post/4de24da3-70a1-4a57-8df8-7d8bd8ef2b70/saffy.png")
        imageList.add(uri) //0번쨰 위치에 임의의 사진 추가
        Log.d(TAG, "initData: $uri")
        getBoardObserver()
        getBoard()
    }

    private fun initData(board: Board){
        insertIndex = board.imageList.size
        Log.d(TAG, "initData213: ${board.imageList}")
        binding.apply {
            if (!isInsert){
                for (image in board.imageList){
                    val uri = Uri.parse(image)
                    imageList.add(uri)
                }
                isInsert = true
            }
            initPhotoAdapter()
            etTitle.setText(board.title) //글 제목
            etContent.setText(board.content) //글 내용
        }
    }

    private fun initPhotoAdapter() {
        photoAdapter = PhotoAdapter(imageList)

        photoAdapter.setItemClickListener(object : PhotoAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int) {
                imageList.removeAt(position)
                photoAdapter.notifyItemRemoved(position)
                insertIndex--
            }
        })

        binding.rvPhoto.apply {
            adapter = photoAdapter
            //원래의 목록위치로 돌아오게함
            photoAdapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    //마케팅정보 동의 다이얼로그 생성
    private fun showDialog(){
        val dialog = AlertDialog(requireActivity() as AppCompatActivity)

        dialog.setOnOKClickedListener {
            val board = boardViewModel.board.value?.data
            binding.apply {
                for (i in 0 until insertIndex){
                    imageList.removeAt(0)
                }
                Log.d(TAG, "showDialog: $insertIndex $imageList")
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO) {
                        val urlList: ArrayList<String> = List(imageList.size) { i ->
                            "IMAGE_${board?.boardId}_${i}.png"
                        } as ArrayList<String>
                        val photoList = GalleryUtils.insertImage(urlList, imageList, 0)
                        board?.imageList?.addAll(photoList)
                        Log.d(TAG, "showDialog: ${board?.imageList}")
                        board?.title = etTitle.text.toString()
                        board?.content = etContent.text.toString()
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        updateBoardObserver()
                    }
                    updateBoard(board!!)
                }
            }
        }

        dialog.show("게시물 수정","게시물을 수정하시겠습니까?")
    }

    //게시물 데이터 받아오기
    private fun getBoard() {
        CoroutineScope(Dispatchers.IO).launch {
            boardViewModel.getBoard(boardViewModel.boardId)
        }
    }

    private fun getBoardObserver() {
        boardViewModel.board.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    initData(it.data!!)
                    boardViewModel.boardId = it.data!!.boardId
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }

    //게시물 수정
    private fun updateBoard(board: Board) {
        Log.d(TAG, "updateBoard: 실행")
        CoroutineScope(Dispatchers.IO).launch {
            boardViewModel.updateBoard(boardViewModel.boardId,board)
        }
    }

    private fun updateBoardObserver() {
        Log.d(TAG, "updateBoardObserver: adkwqodkqo")
        boardViewModel.updateBoard.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    navigationViewModel.type = 4
                    findNavController().navigate(R.id.action_updateBoardFragment_to_mainFragment)
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }
}