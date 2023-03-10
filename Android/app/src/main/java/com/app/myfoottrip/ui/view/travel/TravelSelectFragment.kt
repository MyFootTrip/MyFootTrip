package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.model.Travel
import com.app.myfoottrip.model.VisitPlace
import com.app.myfoottrip.viewmodel.TravelActivityViewModel
import com.app.myfoottrip.viewmodel.TravelViewModel
import com.app.myfoottrip.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentTravelSelectBinding
import com.app.myfoottrip.repository.VisitPlaceRepository
import com.app.myfoottrip.ui.adapter.TravelAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.AlertDialog
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import kotlinx.coroutines.*

private const val TAG = "TravelSelectFragment_싸피"

class TravelSelectFragment : BaseFragment<FragmentTravelSelectBinding>(
    FragmentTravelSelectBinding::inflate
) {
    // ViewModel
    private val travelViewModel by viewModels<TravelViewModel>()

    // ActivityViewModel
    private val travelActivityViewModel by activityViewModels<TravelActivityViewModel>()

    private var type = 0 // 0 : 여정 기록, 1 : 마이페이지, 2 : 게시글 작성
    private val userViewModel by activityViewModels<UserViewModel>()
    private lateinit var visitPlaceRepository: VisitPlaceRepository
    private lateinit var travelAdapter: TravelAdapter
    private var bundle = bundleOf("type" to 3)
    private var boardList = ArrayList<Travel>()

    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    } // End of onAttach

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 유저 생성 ResponseLiveData 다시 초기화
        travelViewModel.setCreateTravelResponseLiveData(NetworkResult.Success(0))
        travelViewModel.setDeleteTravelResponseLiveData(NetworkResult.Success(0))

        //type 받는 코드
        type = requireArguments().getInt("type")
        travelViewModel.setUserTravelDataNewOrUpdateCheck(null)
        // 혹시 모를 SQLLite DB를 항상 비워야함
        CoroutineScope(Dispatchers.IO).launch {
            try {
                visitPlaceRepository.deleteAllVisitPlace()
            } catch (exception: Exception) {
                Log.e(TAG, "onViewCreated: DB에 비울 값이 없습니다.")
                exception.printStackTrace()
            }
        }

        // 남아있는 데이터 확인
        var temp: List<VisitPlace> = emptyList()
        CoroutineScope(Dispatchers.IO).launch {
            val deffered2: Deferred<Int> = async {
                try {
                    temp = visitPlaceRepository.getAllVisitPlace()
                } catch (E: Exception) {
                    Log.e(TAG, "onViewCreated: error")
                    E.printStackTrace()
                }
                1
            }
            deffered2.await()
        }
        userTravelDataObserver()
        userTraveLDataDeleteObserve()
        buttonSetTextObserve()


        initCustomView()
        setListener()

        CoroutineScope(Dispatchers.Default).launch {
            getData()
        }

        initAdapter()

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }


        binding.ivTravelHelp.setOnClickListener {
            findNavController().navigate(R.id.action_travelSelectFragment_to_travelHelpFragment)
        }



        travelAdapter.notifyDataSetChanged()
    } // End of onViewCreated


    private fun buttonSetTextObserve() {
        travelViewModel.userTravelDataNewOrUpdateCheck.observe(viewLifecycleOwner) {
            if (it == null) {
                // Nothing
                bundle = bundleOf(
                    "type" to 0
                )
            }

            if (it == true) {
                bundle = bundleOf("type" to 1)
            }

            if (it == false) {
                bundle = bundleOf(
                    "type" to 2, "travelId" to boardList[travelAdapter.getSelected()].travelId
                )
            }
        }
    } // End of buttonSetTextObserve

    private fun initCustomView() {
        if (type == 0) {
            //여행 선택 페이지
            binding.tvTravelTitle.setText(R.string.plz_travel_select_button_text)
            binding.btnSave.visibility = View.VISIBLE
            binding.btnSave.setText(R.string.make_new_travel_button_text)
            binding.ivTravelHelp.visibility = View.VISIBLE
        } else if (type == 1) {
            // 만들어진 여행 관리 부분
            binding.tvTravelTitle.setText(R.string.select_travel_title)
            binding.btnSave.visibility = View.GONE
        } else {
            //게시글
            binding.btnSave.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.gray_400))
            binding.tvTravelTitle.setText(R.string.select_travel_title)
            binding.btnSave.setText(R.string.plz_travel_select_button_text)
            binding.btnSave.isEnabled = false
        }
    } // End of initCustomView

    private fun initAdapter() {
        travelAdapter = TravelAdapter(type)
        binding.rvTravel.adapter = travelAdapter
        binding.rvTravel.itemAnimator = null

        travelAdapter.setItemClickListener(object : TravelAdapter.ItemClickListener {
            override fun onAllClick(position: Int, travelDto: Travel) {
                //TODO : 여정 확인 페이지로 이동
            }

            override fun onChipClick(type: Int, position: Int, travelDto: Travel) {
                if (type == 0 || type == 2) { //여정 선택
                    changeSelected(position)
                } else { //여정 삭제
                    //TODO : 여정 삭제 dialog
                }
            }

            // Travel 데이터 삭제
            override fun onDeleteChipClick(position: Int, travelDto: Travel) {
                val dialog = AlertDialog(requireActivity() as AppCompatActivity)

                dialog.setOnOKClickedListener {
                    binding.apply {
                        CoroutineScope(Dispatchers.IO).launch {
                            travelViewModel.userTravelDataDelete(boardList[position].travelId!!)

                            withContext(Dispatchers.IO) {
                                getData()
                            }
                        }
                    }
                }

                dialog.setOnCancelClickedListener { }

                dialog.show("여정 삭제", "기록한 여정을 삭제 하시겠습니까?")
            }
        })
    } // End of initAdapter

    private fun setListener() {
        // 여행 생성인지, 기존의 데이터를 불러와서 수정을 하는건지. 구분해야됨.

        binding.apply {
            btnSave.setOnClickListener {
                when (type) {
                    0 -> {
                        findNavController().navigate(
                            R.id.action_travelSelectFragment_to_travelLocationSelectFragment, bundle
                        )
                    }
                    2 -> {
                        val data = bundleOf(
                            "travelId" to boardList[travelAdapter.getSelected()].travelId
                        )
                        findNavController().navigate(
                            R.id.action_travelSelectFragment_to_createBoardFragment, data
                        )

                    }
                }
            }
        }
    } // End of setListener

    private fun userTravelDataObserver() {
        travelViewModel.travelUserData.observe(this.viewLifecycleOwner) {
            binding.travelSelectProgressbar.visibility = View.GONE
            binding.travelSelectProgressbar.isVisible = false

            when (it) {
                is NetworkResult.Success -> {
                    if (it.data != null) {
                        boardList = ArrayList()
                        boardList.addAll(it.data!!)

                        binding.rvTravel.visibility = View.VISIBLE
                        travelAdapter.setList(boardList)
                        travelAdapter.notifyDataSetChanged()
                    }
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "userTravel 체크 Error: ${it.data}")
                }
                is NetworkResult.Loading -> {
                    binding.travelSelectProgressbar.visibility = View.VISIBLE
                    binding.travelSelectProgressbar.isVisible = true
                }
            }
        }
    } // End of userTravelDataObserver

    private fun userTraveLDataDeleteObserve() {
        travelViewModel.userTravelDataDeleteResponseLiveData.observe(this.viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "userTraveLDataDeleteObserve: 이거 왜 돌음")
                    Log.d(TAG, "userTraveLDataDeleteObserve: ${it.data}")

                    if (it.data == 204) {
                        // requireView().showSnackBarMessage("해당 여정이 삭제되었습니다")
                        travelAdapter.notifyDataSetChanged()
                        // 유저 삭제 ResponseLiveData 다시 초기화
                        travelViewModel.setDeleteTravelResponseLiveData(NetworkResult.Success(0))
                    }
                }
                is NetworkResult.Error -> {
                    requireView().showSnackBarMessage("유저 여행 데이터 삭제 오류 발생")
                    Log.d(TAG, "createTravelResponseLiveData Error: ${it.data}")
                    Log.d(TAG, "createTravelResponseLiveData Error: ${it.message}")
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "createTravelResponseLiveData Loading")
                }
            }
        }
    } // End of userTraveLDataDeleteObserve

    // 유저 데이터 가져와서 저장.
    private suspend fun getData() {
        CoroutineScope(Dispatchers.IO).launch {
            userViewModel.wholeMyData.value?.uid?.let { travelViewModel.getUserTravel(it) }
        }
    } // End of getData

    private fun changeSelected(position: Int) {
        travelAdapter.run {
            var lastPos = getSelected()
            if (position == lastPos) { //선택 해제
                setSelected(-1)
                settingView(false)
            } else { //선택
                setSelected(position)
                settingView(true)
            }
        }
    } // End of changeSelected

    private fun settingView(isChecked: Boolean) {
        if (isChecked) {
            when (type) {
                0 -> {
                    // 기존의 여행 데이터 수정
                    travelViewModel.setUserTravelDataNewOrUpdateCheck(false)
                    binding.btnSave.setText("선택 완료")
                }
                2 -> {
                    binding.btnSave.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(), R.color.main
                        )
                    )
                    binding.btnSave.setText("게시글  작성하기")
                    binding.btnSave.isClickable = true
                    binding.btnSave.isEnabled = true
                }
            }
        } else {
            when (type) {
                0 -> {
                    // 새로운 여행 데이터 생성
                    travelViewModel.setUserTravelDataNewOrUpdateCheck(true)
                    binding.btnSave.setText(R.string.make_new_travel_button_text)
                    binding.btnSave.isClickable = true
                    binding.btnSave.isEnabled = true
                }
                2 -> {
                    binding.btnSave.setText(R.string.plz_travel_select_button_text)
                    binding.btnSave.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(), R.color.gray_400
                        )
                    )
                    binding.btnSave.isClickable = false
                    binding.btnSave.isEnabled = false
                }
            }
        }
    } // End of settingView

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    } // End of onDetach

} // End of TravelSelectFragment class
