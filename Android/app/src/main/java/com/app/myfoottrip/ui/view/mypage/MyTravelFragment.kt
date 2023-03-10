package com.app.myfoottrip.ui.view.mypage

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.model.Travel
import com.app.myfoottrip.viewmodel.NavigationViewModel
import com.app.myfoottrip.viewmodel.TravelActivityViewModel
import com.app.myfoottrip.viewmodel.TravelViewModel
import com.app.myfoottrip.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentMyTravelBinding
import com.app.myfoottrip.repository.VisitPlaceRepository
import com.app.myfoottrip.ui.adapter.MyTravelAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.dialogs.AlertDialog
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MyTravelFragment_마이풋트립"

class MyTravelFragment : BaseFragment<FragmentMyTravelBinding>(
    FragmentMyTravelBinding::inflate
) {

    private lateinit var mainActivity: MainActivity

    private val navigationViewModel by activityViewModels<NavigationViewModel>()

    // ViewModel
    private val travelViewModel by viewModels<TravelViewModel>()

    // ActivityViewModel
    private val travelActivityViewModel by activityViewModels<TravelActivityViewModel>()

    private val userViewModel by activityViewModels<UserViewModel>()
    private lateinit var visitPlaceRepository: VisitPlaceRepository
    private lateinit var myTravelAdapter: MyTravelAdapter
    private var boardList = ArrayList<Travel>()

    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigationViewModel.type = 1
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    } // End of onAttach

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()

        init()

        travelViewModel.setUserTravelDataNewOrUpdateCheck(null)

        binding.apply {
            ivBack.setOnClickListener {
                navigationViewModel.type = 1
                findNavController().popBackStack()
            } //뒤로가기
        }
    }


    override fun onDetach() {
        super.onDetach()
        callback.remove()
    } // End of onDetach

    private fun init() {
        CoroutineScope(Dispatchers.Default).launch {
            setData()
        }

        initAdapter()
    }

    private fun initAdapter() {
        myTravelAdapter = MyTravelAdapter()

        binding.rvTravel.adapter = myTravelAdapter
        binding.rvTravel.itemAnimator = null
        myTravelAdapter.setItemClickListener(object : MyTravelAdapter.ItemClickListener {
            override fun onAllClick(position: Int, travelDto: Travel) {
            }

            override fun onChipClick(type: Int, position: Int, travelDto: Travel) {
            }

            override fun onDeleteChipClick(position: Int, travelDto: Travel) {
                // 선택된 포지션의 값을 가져와서 해당 값을 제거해야됨
                // 서버에 삭제 요청을 보내야 함.
                val dialog = AlertDialog(requireActivity() as AppCompatActivity)

                dialog.setOnOKClickedListener {
                    binding.apply {
                        CoroutineScope(Dispatchers.IO).launch {
                            travelViewModel.userTravelDataDelete(boardList[position].travelId!!)

                            // 삭제를 마치고 나면 data를 다시 갱신해야함
                            withContext(Dispatchers.Default) {
                                setData()
                            }
                        }
                    }
                }

                dialog.setOnCancelClickedListener { }

                dialog.show("여정 삭제", "여정을 삭제하시겠습니까?")

            }
        })
    } // End of initAdapter

    private fun initObserver() {
        userTravelDataObserver()
    }

    private fun userTravelDataObserver() {
        travelViewModel.travelUserData.observe(viewLifecycleOwner) {

            when (it) {
                is NetworkResult.Success -> {
                    if (it.data != null) {
                        boardList = ArrayList()
                        boardList.addAll(it.data!!)
                        if (boardList.isNullOrEmpty()) binding.tvTravelExist.visibility =
                            View.VISIBLE
                        else binding.tvTravelExist.visibility = View.INVISIBLE
                        myTravelAdapter.setList(boardList)
                        myTravelAdapter.notifyDataSetChanged()
                    }
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "userTravel 체크 Error: ${it.data}")
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    } // End of userTravelDataObserver

    private suspend fun setData() {
        CoroutineScope(Dispatchers.IO).launch {
            userViewModel.wholeMyData.value?.uid?.let { travelViewModel.getUserTravel(it) }
        }
    } // End of setData
}