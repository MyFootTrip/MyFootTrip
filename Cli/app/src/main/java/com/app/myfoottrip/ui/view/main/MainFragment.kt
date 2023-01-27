package com.app.myfoottrip.ui.view.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.TokenViewModel
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
    private val tokenViewModel by activityViewModels<TokenViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    } // End of onAttack

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initNavigation()

        binding.apply {
            bottomNavigationView.background = null // 음영 겹치는거 제거
            bottomNavigationView.menu.getItem(1).isEnabled = false //가운데 아이템 선택 불가능
        }


        // 페이지에 들어오면 유저 정보를 가져옴
        /*
            1. accessToken을 통해서 먼저 유저 정보를 가져온다.
            2. accessToken이 만료되었을 경우 refreshToken을 통해서 accessToken을 다시 발급받는다.
            3. refreshToken도 만료되었을 경우, 로그아웃 처리된다.
         */
        getUserMyData()

        getAccessTokenByRefreshTokenResponseLiveDataObserver()
    } // End of onViewCreated

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
                    R.id.action_mainFragment_to_travelSelectFragment, bundle
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

    private fun getUserMyData() {
        CoroutineScope(Dispatchers.IO).launch {
            tokenViewModel.getUserDataByAccessToken()
            Log.d(TAG, "getUserMyData: 얘가 돌음?")
        }
    } // End of getUserMyData

    private fun getAccessTokenByRefreshTokenResponseLiveDataObserver() {
        Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: 옵저버 돌음?")

        tokenViewModel.getUserDataByAccessTokenResponseLiveData.observe(viewLifecycleOwner) {
            Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: 옵저버 들어감?")

            when (it) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "여기는 됨: ${it.data}")
                    userViewModel.setWholeMyData(it.data!!)
                    Log.d(TAG, "여기 나옴: ${it.data}")
                    Log.d(TAG, " 뷰 모델 저장 : ${userViewModel.wholeMyData.value} ")
                }

                is NetworkResult.Error -> {
                    // AccessToken을 통해서 유저 정보를 가져오기 실패했는지 파악해야됨.
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: 토큰 만료됨")

                    // RefreshToken을 통해서 AccessToken을 재발급
                }

                is NetworkResult.Loading -> {
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: 로딩 중입니다")
                }
            }
        }

    } // End of getAccessTokenByRefreshTokenResponseLiveDataObserver
} // End of MainFragment class
