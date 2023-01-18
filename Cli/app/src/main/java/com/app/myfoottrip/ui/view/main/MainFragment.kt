package com.app.myfoottrip.ui.view.main

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.FragmentMainBinding
import com.app.myfoottrip.ui.base.BaseFragment

class MainFragment : BaseFragment<FragmentMainBinding>(
    FragmentMainBinding::bind, R.layout.fragment_main
) {

    private lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        binding.apply {
            bottomNavigationView.background = null // 음영 겹치는거 제거
            bottomNavigationView.menu.getItem(1).isEnabled = false //가운데 아이템 선택 불가능
        }
    }

    private fun init(){
        initNavigation()
    }

    //바텀 네비게이션 설정
    private fun initNavigation() {
        binding.apply {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_bottom_fragment, HomeFragment()).commit()

            bottomNavigationView.setOnItemSelectedListener  {
                navigationSelected(it)
            }
            addButton.setOnClickListener{
                findNavController().navigate(R.id.action_mainFragment_to_travelSelectFragment)
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
}