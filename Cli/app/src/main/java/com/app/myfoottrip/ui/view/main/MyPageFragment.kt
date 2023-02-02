package com.app.myfoottrip.ui.view.main

import android.animation.Animator
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentMyPageBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.util.SharedPreferencesUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "MyPageFragment_마이풋트립"

class MyPageFragment : BaseFragment<FragmentMyPageBinding>(
    FragmentMyPageBinding::bind, R.layout.fragment_my_page
) {

    private val userViewModel by activityViewModels<UserViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        binding.apply {
            // 내 여정 페이지로 이동
            cvMyTravel.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_myTravelFragment)
            }
            // 개인정보수정 페이지로 이동
            llToEditAccount.setOnClickListener{
                findNavController().navigate(R.id.action_mainFragment_to_editAccountFragment)
            }
            // 내가 작성한 게시글 목록 페이지로 이동
            // 내가 좋아요한 게시글 목록 페이지로 이동
            clMoveLike.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_myLikeFragment)
            }
        }
    }

    private fun init(){
        initUser()
        binding.ivILikedImage.setMaxProgress(0.6f) //좋아요 애니메이션 세팅
    }

    //유저정보 데이터 초기화
    private fun initUser(){
        binding.apply {
            //프로필 이미지
            if (userViewModel.wholeMyData.value?.join?.profile_image.isNullOrEmpty()){
                ivProfile.setPadding(30)
                Glide.with(this@MyPageFragment).load(R.drawable.ic_my).fitCenter().into(ivProfile)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white)))
            }else {
                Glide.with(this@MyPageFragment).load(userViewModel.wholeMyData.value?.join?.profile_image).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).thumbnail(Glide.with(this@MyPageFragment).load(R.drawable.loading_image).centerCrop()).centerCrop().into(ivProfile)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white)))
            }

            textView.text = "${userViewModel.wholeMyData.value?.join?.nickname}님" //닉네임
            tvMyTravelCnt.text = "${userViewModel.wholeMyData.value?.travel?.size}개" //현재 작성하고 있는 여정의 수
            tvIWroteCnt.text = "${userViewModel.wholeMyData.value?.writeBoard?.size}개" //내가 작성한 게시글 수
            tvILikedCnt.text = "${userViewModel.wholeMyData.value?.myLikeBoard?.size}개" //좋아요 한 게시글 수
        }
    }

}