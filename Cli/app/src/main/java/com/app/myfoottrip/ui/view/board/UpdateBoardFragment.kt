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
import com.app.myfoottrip.util.showSnackBarMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "UpdateBoardFragment_마이풋트립"

class UpdateBoardFragment : BaseFragment<FragmentUpdateBoardBinding>(
    FragmentUpdateBoardBinding::bind, R.layout.fragment_update_board
) {
    private lateinit var mainActivity: MainActivity
    private val navigationViewModel by activityViewModels<NavigationViewModel>()
    private lateinit var callback: OnBackPressedCallback

    private val boardViewModel by activityViewModels<BoardViewModel>()
    private lateinit var photoAdapter: PhotoAdapter
    private var imageList = ArrayList<Uri>()

    private var isInsert = false
    private var currentTheme = ""

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

    private fun init() {
        getBoardObserver()
        getBoard()
        initChips()
    }

    private fun initData(board: Board) {
        binding.apply {
            if (!isInsert) {
                for (i in board.imageList.indices) {
                    val uri = Uri.parse(board.imageList[i])
                    imageList.add(uri)
                }
                isInsert = true
            }

            initPhotoAdapter()
            etTitle.setText(board.title) //글 제목
            etContent.setText(board.content) //글 내용
            initTheme(board.theme) //테마
            currentTheme = board.theme
        }
    }

    private fun initPhotoAdapter() {
        photoAdapter = PhotoAdapter(imageList)

        photoAdapter.setItemClickListener(object : PhotoAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int) {
                imageList.removeAt(position)
                boardViewModel.board.value?.data?.imageList?.removeAt(position)
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

    //테마 데이터 갱신
    private fun initTheme(theme: String){
        binding.apply {
            when(theme){
                "혼자" -> chipSingle.isChecked = true
                "친구와" -> chipFriend.isChecked = true
                "연인과" -> chipCouple.isChecked = true
                "배우자와" -> chipSpouse.isChecked = true
                "아이와" -> chipKid.isChecked = true
                "부모님과" -> chipParent.isChecked = true
                "기타" -> chipEtc.isChecked = true
            }
        }
    }

    //테마 클릭 세팅
    private fun initChips(){
        binding.apply {
            cgCategory.setOnCheckedChangeListener { group, checkedId ->
                when(checkedId){
                    R.id.chip_single -> {
                        currentTheme = "혼자"
                        cgCategory2.clearCheck()
                    }
                    R.id.chip_friend ->{
                        currentTheme = "친구와"
                        cgCategory2.clearCheck()
                    }
                    R.id.chip_couple -> {
                        currentTheme = "연인과"
                        cgCategory2.clearCheck()
                    }
                    R.id.chip_spouse -> {
                        currentTheme = "배우자와"
                        cgCategory2.clearCheck()
                    }
                }
            }
            cgCategory2.setOnCheckedChangeListener { group, checkedId ->
                when(checkedId){
                    R.id.chip_kid -> {
                        currentTheme = "아이와"
                        cgCategory.clearCheck()
                    }
                    R.id.chip_parent -> {
                        currentTheme = "부모님과"
                        cgCategory.clearCheck()
                    }
                    R.id.chip_etc -> {
                        currentTheme = "기타"
                        cgCategory.clearCheck()
                    }
                }
            }
        }
    }

    //게시물 수정 다이얼로그 생성
    private fun showDialog() {
        val dialog = AlertDialog(requireActivity() as AppCompatActivity)

        dialog.setOnOKClickedListener {
            binding.apply {

                imageList.removeAt(0) //더미데이터 지우기
                boardViewModel.board.value?.data?.imageList?.clear()

                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO) {
                        val urlList: ArrayList<String> = List(imageList.size) { i ->
                            "IMAGE_${boardViewModel.boardId}_${i}.png"
                        } as ArrayList<String>
                        if (imageList.isNotEmpty()) {
                            val photoList = GalleryUtils.insertImage(urlList, imageList, 0,boardViewModel.boardId)
                            boardViewModel.board.value?.data?.imageList?.addAll(photoList)
                        }
                    }
                    boardViewModel.board.value?.data?.title = etTitle.text.toString()
                    boardViewModel.board.value?.data?.content = etContent.text.toString()
                    boardViewModel.board.value?.data?.theme = currentTheme

                    CoroutineScope(Dispatchers.Main).launch {
                        updateBoardObserver()
                    }

                    updateBoard(boardViewModel.board.value?.data!!)
                }
            }
        }

        dialog.setOnCancelClickedListener { }

        dialog.show("게시물 수정", "게시물을 수정하시겠습니까?")
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
                    it.data!!.imageList.add(0, "https://images.velog.io/images/ccmmss98/post/4de24da3-70a1-4a57-8df8-7d8bd8ef2b70/saffy.png")
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
        CoroutineScope(Dispatchers.IO).launch {
            boardViewModel.updateBoard(boardViewModel.boardId, board)
        }
    }

    private fun updateBoardObserver() {
        boardViewModel.updateBoard.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    navigationViewModel.type = 4
                    findNavController().navigate(R.id.action_updateBoardFragment_to_mainFragment)
                    binding.root.showSnackBarMessage("게시물이 수정되었습니다.")
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }
}