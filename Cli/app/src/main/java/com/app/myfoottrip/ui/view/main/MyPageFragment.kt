package com.app.myfoottrip.ui.view.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.User
import com.app.myfoottrip.data.viewmodel.TokenViewModel
import com.app.myfoottrip.data.viewmodel.UserViewModel
import com.app.myfoottrip.databinding.FragmentMyPageBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.util.NetworkResult
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.*

private const val TAG = "MyPageFragment_마이풋트립"

class MyPageFragment : BaseFragment<FragmentMyPageBinding>(
    FragmentMyPageBinding::bind, R.layout.fragment_my_page
) {

    private val tokenViewModel by activityViewModels<TokenViewModel>()

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
            clWrite.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_myWriteFragment)
            }
            // 내가 좋아요한 게시글 목록 페이지로 이동
            clMoveLike.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_myLikeFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init(){
        binding.ivILikedImage.setMaxProgress(0.6f) //좋아요 애니메이션 세팅
        getAccessTokenByRefreshTokenResponseLiveDataObserver()
        getUserMyData()
    }


    //유저정보 데이터 초기화
    private fun initUser(user: User){
        binding.apply {
            //프로필 이미지
            if (user.join.profile_image.isNullOrEmpty()){
                ivProfile.setPadding(30)
                Glide.with(this@MyPageFragment).load(R.drawable.ic_my).fitCenter().into(ivProfile)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white)))
            }else {
                Glide.with(this@MyPageFragment).load(user.join.profile_image).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).thumbnail(Glide.with(this@MyPageFragment).load(R.drawable.loading_image).centerCrop()).centerCrop().into(ivProfile)
                cvProfileLayout.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.white)))
            }
            textView.text = "${user.join.nickname}님" //닉네임
            tvMyTravelCnt.text = "${user.travel.size}개" //현재 작성하고 있는 여정의 수
            tvIWroteCnt.text = "${user.writeBoard.size}개" //내가 작성한 게시글 수
            tvILikedCnt.text = "${user.myLikeBoard.size}개" //좋아요 한 게시글 수
        }
    }

    private fun getUserMyData() {
        CoroutineScope(Dispatchers.IO).launch {
            tokenViewModel.getUserData()
        }
    } // End of getUserMyData

    private fun getAccessTokenByRefreshTokenResponseLiveDataObserver() {
        tokenViewModel.getUserResponseLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                        initUser(it.data!!)
//                        userViewModel.setWholeMyData(it.data!!)
                }
                is NetworkResult.Error -> {
                    // AccessToken을 통해서 유저 정보를 가져오기 실패했는지 파악해야됨.
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: 토큰 만료됨")
                    // RefreshToken을 통해서 AccessToken을 재발급
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: ${it.data}")
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: ${it.message}")
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "getAccessTokenByRefreshTokenResponseLiveDataObserver: 로딩 중입니다")
                }
            }
        }
    } // End of getAccessTokenByRefreshTokenResponseLiveDataObserver

}