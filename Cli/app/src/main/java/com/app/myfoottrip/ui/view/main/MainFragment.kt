package com.app.myfoottrip.ui.view.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.User
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentMainBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.util.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val TAG = "MainFragment_싸피"

class MainFragment : BaseFragment<FragmentMainBinding>(
    FragmentMainBinding::bind, R.layout.fragment_main
) {

    private lateinit var mainActivity: MainActivity
    private val userViewModel by activityViewModels<UserViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    } // End of onAttack

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        binding.apply {
            bottomNavigationView.background = null // 음영 겹치는거 제거
            bottomNavigationView.menu.getItem(1).isEnabled = false //가운데 아이템 선택 불가능
        }


        CoroutineScope(Dispatchers.IO).launch {
            userViewModel.getUserDataByAccessToken()
        }

        getUserDataResponseLiveDataObserve()
    } // End of onViewCreated

    private fun init() {
        initNavigation()
    }

    //바텀 네비게이션 설정
    private fun initNavigation() {
        binding.apply {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_bottom_fragment, HomeFragment()).commit()

            bottomNavigationView.setOnItemSelectedListener {
                navigationSelected(it)
            }
            addButton.setOnClickListener { //여정 기록 -> 여정 선택 화면
                val bundle = bundleOf("type" to 0)
                findNavController().navigate(
                    R.id.action_mainFragment_to_travelSelectFragment,
                    bundle
                )
            }
        }
    }

    //바텀네비게이션 아이템 선택 시 화면 이동
    private fun navigationSelected(item: MenuItem): Boolean {
        binding.apply {
            val checked = item.setChecked(true)
            when (checked.itemId) {
                R.id.homeFragment -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_bottom_fragment, HomeFragment()).commit()
                    true
                }
                R.id.mypageFragment -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_bottom_fragment, MyPageFragment()).commit()
                    true
                }
            }
            return false
        }
    }

    private fun getUserDataResponseLiveDataObserve() {
        userViewModel.getUserDataResponseLiveData.observe(viewLifecycleOwner) {

            when (it) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "getUserDataResponseLiveDataObserve 성공 : ${it.data} ")
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "이메일 체크 Error: ${it.data}")
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "emailValidateCheckObserver: 로딩 중")
                }
            }
        }

    } // End of getUserDataResponseLiveDataObserve
} // End of MainFragment class
