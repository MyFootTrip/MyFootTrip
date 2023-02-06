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
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.data.viewmodel.BoardViewModel
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.databinding.FragmentCreateBoardBinding
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
import java.util.*


private const val TAG = "CreateBoardFragment_마이풋트립"

class CreateBoardFragment : BaseFragment<FragmentCreateBoardBinding>(
    FragmentCreateBoardBinding::bind, R.layout.fragment_create_board
) {
    private lateinit var mainActivity: MainActivity

    private lateinit var photoAdapter: PhotoAdapter
    private var imageList = ArrayList<Uri>()

    private val boardViewModel by activityViewModels<BoardViewModel>()

    private var currentTheme = ""
    private var travelId = -1

    private lateinit var callback: OnBackPressedCallback
    private val navigationViewModel by activityViewModels<NavigationViewModel>()

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
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        travelId = requireArguments().getInt("travelId")

        init()

        binding.apply {
            ivBack.setOnClickListener { findNavController().popBackStack() } //뒤로가기
            photoAddBtn.setOnClickListener { //갤러리 이미지 불러오기 버튼
                GalleryUtils.getGallery(requireContext(), imageLauncher)
            }

            //게시물 등록하기 버튼
            btnCreate.setOnClickListener {
                if (cgCategory.checkedChipId == -1 && cgCategory2.checkedChipId == -1){
                    binding.root.showSnackBarMessage("테마를 선택해주세요!")
                }else{
                    showDialog()
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

    private fun init() {
        val uri = Uri.parse("https://images.velog.io/images/ccmmss98/post/4de24da3-70a1-4a57-8df8-7d8bd8ef2b70/saffy.png")
        imageList.add(uri)
        initChips()
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

    //게시물 생성 다이얼로그 생성
    private fun showDialog() {
        val dialog = AlertDialog(requireActivity() as AppCompatActivity)

        dialog.setOnOKClickedListener {
            binding.apply {

                //로딩창 구현
                mainActivity.runOnUiThread {
                    binding.root.isClickable = false
                    scrollCreateBoard.visibility = View.INVISIBLE
                    lottieCreateBoard.visibility = View.VISIBLE
                    lottieCreateBoard.playAnimation()
                }

                val board = Board(1, 1, "테스트계정", "string", Date(System.currentTimeMillis()), "혼자놀기", "임시제목입니다", "임시 내용입니다.", arrayListOf(), Travel(travelId,
                    arrayListOf(), Date(System.currentTimeMillis()),
                    Date(System.currentTimeMillis()),
                    arrayListOf()), arrayListOf(), arrayListOf())
                imageList.removeAt(0)
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.IO) {
                        val urlList: ArrayList<String> = List(imageList.size) { i ->
                            "IMAGE_${board.boardId}_${i}.png"
                        } as ArrayList<String>
                        board.imageList = GalleryUtils.insertImage(urlList, imageList, 0, board.boardId)
                        board.title = etTitle.text.toString()
                        board.content = etContent.text.toString()
                        board.theme = currentTheme
                    }

                    createBoard(board)

                    CoroutineScope(Dispatchers.Main).launch {
                        createBoardObserver()
                    }
                }
            }
        }

        dialog.setOnCancelClickedListener { }

        dialog.show("게시물 작성", "게시물을 작성하시겠습니까?")
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
                    navigationViewModel.type = 4
                    findNavController().navigate(R.id.action_createBoardFragment_to_mainFragment)
                    binding.root.showSnackBarMessage("게시물이 작성되었습니다.")
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