package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.Application
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.FcmViewModel
import com.app.myfoottrip.data.viewmodel.JoinViewModel
import com.app.myfoottrip.databinding.FragmentJoinAgeBinding
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.NetworkResult
import com.app.myfoottrip.util.showToastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "JoinAgeFragment_싸피"

class JoinAgeFragment : Fragment() {
    private lateinit var binding: FragmentJoinAgeBinding
    private lateinit var mContext: Context
    private val joinViewModel by activityViewModels<JoinViewModel>()
    private lateinit var joinBackButtonCustomView: JoinBackButtonCustomView
    private lateinit var imageViewArray: Array<ImageView>
    private lateinit var selectedImageViewArray: Array<Int>
    private lateinit var nonSelectedImageViewArray: Array<Int>
    private val fcmViewModel by activityViewModels<FcmViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinAgeBinding.inflate(
            inflater, container, false
        )
        return binding.root
    } // End of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 처음 데이터 호출
        initData()

        // 연령선택 이벤트
        ageButtonClick()

        // 연령선택 상태값
        ageStateObserver()

        // 회원가입 성공 여부를 저장하는 viewModel 값을 관찰하는 옵저버 등록
        joinResponseLiveDataObserver()

        // 회원가입 버튼 클릭 이벤트
        binding.joinNextButton.setOnClickListener {
            binding.joinProgressbar.visibility = View.VISIBLE
            binding.ageGirdlayout.visibility = View.INVISIBLE
            val temp = joinViewModel.ageState.value
            val size = temp!!.size

            for (i in 0 until size) {
                if (temp[i]) {
                    joinViewModel.wholeJoinUserData.age = ((i + 1) * 10).toString()
                    break
                }
            }

            // 전체 viewModel에 저장된 값을 multipart/form-data로 변경해서 통신해야함
            CoroutineScope(Dispatchers.IO).launch {
                joinViewModel.userJoin()
            }
        }

    } // End of onViewCreated

    private fun initData() {
        joinBackButtonCustomView = binding.joinBackButtonCustomview

        joinBackButtonCustomView.findViewById<AppCompatButton>(R.id.custom_back_button_appcompatbutton)
            .setOnClickListener {
                Log.d(TAG, "joinBackButtonCustomView : onClick")
                findNavController().popBackStack()
            }

        imageViewArray = arrayOf(
            binding.joinAgeButton10,
            binding.joinAgeButton20,
            binding.joinAgeButton30,
            binding.joinAgeButton40,
            binding.joinAgeButton50,
            binding.joinAgeButton60,
        )

        selectedImageViewArray = arrayOf(
            R.drawable.age10_selected,
            R.drawable.age20_selected,
            R.drawable.age30_selected,
            R.drawable.age40_selected,
            R.drawable.age50_selected,
            R.drawable.age60_selected,
        )

        nonSelectedImageViewArray = arrayOf(
            R.drawable.age10_button_non_select,
            R.drawable.age20_button_non_select,
            R.drawable.age30_button_non_select,
            R.drawable.age40_button_non_select,
            R.drawable.age50_button_non_select,
            R.drawable.age60_button_non_select,
        )
    } // End of initData

    private fun ageStateObserver() {
        joinViewModel.ageState.observe(viewLifecycleOwner) {
            // 상태가 바뀌면 해당 상태값 위치의 이미지 변화
            for (i in 0 until 6) {
                if (joinViewModel.ageState.value!![i]) {
                    imageViewArray[i].setBackgroundResource(selectedImageViewArray[i])
                } else {
                    imageViewArray[i].setBackgroundResource(nonSelectedImageViewArray[i])
                }
            }
        }
    } // End of ageStateObserver

    private fun ageButtonClick() {
        binding.joinAgeButton10.setOnClickListener {
            joinViewModel.changeAgeState(0)
        }

        binding.joinAgeButton20.setOnClickListener {
            joinViewModel.changeAgeState(1)
        }

        binding.joinAgeButton30.setOnClickListener {
            joinViewModel.changeAgeState(2)
        }

        binding.joinAgeButton40.setOnClickListener {
            joinViewModel.changeAgeState(3)
        }

        binding.joinAgeButton50.setOnClickListener {
            joinViewModel.changeAgeState(4)
        }

        binding.joinAgeButton60.setOnClickListener {
            joinViewModel.changeAgeState(5)
        }
    } // End of ageButtonClick


    // _joinResponseLiveData
    private fun joinResponseLiveDataObserver() {
        joinViewModel.joinResponseLiveData.observe(viewLifecycleOwner) {
            binding.joinProgressbar.isVisible = false
            binding.joinProgressbar.visibility = View.GONE

            when (it) {
                is NetworkResult.Success -> {
                    mContext.showToastMessage("회원가입 성공")

                    // refreshToken & accessToken
                    // savedInstance 저장하기
                    Application.sharedPreferencesUtil.addUserAccessToken(it.data!!.access_token.toString())
                    Application.sharedPreferencesUtil.addUserRefreshToken("${it.data!!.refresh_token}")

                    addFcmTokenObserver()
                    addFcmToken()
                }
                is NetworkResult.Error -> {
                    Log.d(TAG, "회원가입 실패: ${it.data}")
                    mContext.showToastMessage("회원가입 실패")
                }
                is NetworkResult.Loading -> {
                    binding.joinProgressbar.isVisible = true
                    binding.joinProgressbar.visibility = View.VISIBLE
                }
            }
        }
    } // End of joinResponseLiveDataObserver {

    //FCM 토큰 저장하기
    private fun addFcmToken() {
        CoroutineScope(Dispatchers.IO).launch {
            fcmViewModel.addFcmToken(Application.sharedPreferencesUtil.getFcmToken())
        }
    }

    private fun addFcmTokenObserver() {
        fcmViewModel.addFcmToken.observe(viewLifecycleOwner) {
            when (it) {
                is NetworkResult.Success -> {
                    val intent = Intent(mContext, MainActivity::class.java)
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    activity!!.finish()
                }
                is NetworkResult.Error -> {
                }
                is NetworkResult.Loading -> {
                }
            }
        }
    }
} // End of JoinAgeFragment class
