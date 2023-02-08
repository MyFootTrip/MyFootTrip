package com.app.myfoottrip.ui.view.mypage

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Alarm
import com.app.myfoottrip.data.viewmodel.AlarmViewModel
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentAlarmBinding
import com.app.myfoottrip.ui.adapter.AlarmAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showSnackBarMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val TAG = "AlarmFragment_마이풋트립"

class AlarmFragment : BaseFragment<FragmentAlarmBinding>(
    FragmentAlarmBinding::bind, R.layout.fragment_alarm
){
    private lateinit var mainActivity: MainActivity

    private val userViewModel by activityViewModels<UserViewModel>()
    private val navigationViewModel by activityViewModels<NavigationViewModel>()

    private val alarmViewModel by activityViewModels<AlarmViewModel>()
    private lateinit var alarmAdapter: AlarmAdapter

    private var isDelete = false
    private var deletePosition = -1
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()

        init()

        binding.apply {
            ivBack.setOnClickListener {
                navigationViewModel.type = 1
                findNavController().popBackStack()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
    } // End of onDetach

    private fun init(){
        getAlarmList()
    }

    private fun initObserver(){
        getAlarmListObserver()
        alarmDeleteObserver()
    }
    
    private fun initAlarmAdapter(){

        alarmAdapter = AlarmAdapter(alarmViewModel.alarmList.value?.data!!)

        alarmAdapter.setItemClickListener(object : AlarmAdapter.ItemClickListener {
            override fun onDeleteAlarm(position: Int, alarm: Alarm) {

                CoroutineScope(Dispatchers.IO).launch {
                    alarmDelete(alarm.notificationId)
                    isDelete = true
                    deletePosition = position
                    // 삭제를 마치고 나면 data를 다시 갱신해야함
                    withContext(Dispatchers.Default) {
                        getAlarmList()
                    }
                }
            }
        })

        binding.rvAlarm.apply {
            adapter = alarmAdapter
            //원래의 목록위치로 돌아오게함
            alarmAdapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    private fun getAlarmList(){
        CoroutineScope(Dispatchers.IO).launch {
            alarmViewModel.getAlarmList()
        }
    }

    private fun getAlarmListObserver(){
        alarmViewModel.alarmList.observe(viewLifecycleOwner){
            when (it) {
                is NetworkResult.Success -> {
                    alarmViewModel.alarmList.value?.data = it.data!!
                    if(it.data!!.isNullOrEmpty()) binding.tvAlarmExist.visibility = View.VISIBLE
                    else binding.tvAlarmExist.visibility = View.INVISIBLE
                    initAlarmAdapter()
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }

    private fun alarmDelete(notificationId: Int){
        CoroutineScope(Dispatchers.IO).launch {
            alarmViewModel.alarmDelete(notificationId)
        }
    }

    private fun alarmDeleteObserver(){
        alarmViewModel.alarmDeleteResponseLiveData.observe(viewLifecycleOwner){
            when (it) {
                is NetworkResult.Success -> {
                    if (it.data == 204) {
                        if (isDelete) {
                            requireView().showSnackBarMessage("해당 알림이 삭제되었습니다.")
                            alarmViewModel.alarmList.value?.data!!.removeAt(deletePosition)
                        }
                        alarmAdapter.notifyItemRemoved(deletePosition)
                        initAlarmAdapter()
                    }
                }

                is NetworkResult.Error -> {
                    requireView().showSnackBarMessage("유저 여행 데이터 삭제 오류 발생")
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "createTravelResponseLiveData Loading")
                }
            }
        }
    }

}