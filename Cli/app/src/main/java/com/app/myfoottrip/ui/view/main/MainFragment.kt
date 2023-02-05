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
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentMainBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.skydoves.powerspinner.PowerSpinnerView

private const val TAG = "MainFragment_마이풋트립"

class MainFragment : BaseFragment<FragmentMainBinding>(
    FragmentMainBinding::bind, R.layout.fragment_main
) {

    private val navigationViewModel by activityViewModels<NavigationViewModel>()

    private lateinit var mContext: Context
    private lateinit var mainActivity: MainActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mainActivity = context as MainActivity
    } // End of onAttach

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initNavigation()

        binding.apply {
            bottomNavigationView.background = null // 음영 겹치는거 제거
            bottomNavigationView.menu.getItem(1).isEnabled = false //가운데 아이템 선택 불가능
        }
    } // End of onViewCreated

    override fun onResume() {
        super.onResume()
        if (navigationViewModel.type == 4){
            binding.bottomNavigationView.menu.findItem(R.id.homeFragment).isChecked = true
        }
    }

    //바텀 네비게이션 설정
    private fun initNavigation() {
        binding.apply {
            
            when (navigationViewModel.type) {
                0 -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_bottom_fragment, HomeFragment()).commit()
                }
                1 -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_bottom_fragment, MyPageFragment()).commit()
                }
                4 -> {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_bottom_fragment, HomeFragment()).commit()
                }
            }

            bottomNavigationView.setOnItemSelectedListener {
                navigationSelected(it)
            }

            addButton.setOnClickListener { //여정 기록 -> 여정 선택 화면
                val bundle = bundleOf("type" to 0)
                if (mainActivity.findViewById<PowerSpinnerView>(R.id.spinner_sort) != null){
                    mainActivity.findViewById<PowerSpinnerView>(R.id.spinner_sort).dismiss()
                }
                findNavController().navigate(R.id.action_mainFragment_to_travelSelectFragment, bundle)
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
                    if (mainActivity.findViewById<PowerSpinnerView>(R.id.spinner_sort) != null){
                        mainActivity.findViewById<PowerSpinnerView>(R.id.spinner_sort).dismiss()
                    }
                    true
                }
            }
            return false
        }
    }

} // End of MainFragment class
