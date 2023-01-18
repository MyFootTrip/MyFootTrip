package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.app.myfoottrip.Application
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.JoinTest
import com.app.myfoottrip.data.dto.Token
import com.app.myfoottrip.data.model.viewmodel.JoinViewModel
import com.app.myfoottrip.databinding.FragmentJoinAgeBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity
import com.app.myfoottrip.util.SharedPreferencesUtil
import com.app.myfoottrip.util.showToastMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "JoinAgeFragment_싸피"

class JoinAgeFragment : Fragment() {
    private lateinit var binding: FragmentJoinAgeBinding
    private lateinit var mContext: Context
    private val joinViewModel by activityViewModels<JoinViewModel>()
    private lateinit var imageViewArray: Array<ImageView>
    private lateinit var selectedImageViewArray: Array<Int>
    private lateinit var nonSelectedImageViewArray: Array<Int>
    private lateinit var joinSeccessUserData: JoinTest

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    } // End of onCreate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = com.app.myfoottrip.databinding.FragmentJoinAgeBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    } // End of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 처음 데이터 호출
        initData()

        ageButtonClick()

        ageStateObserver()

        // 회원가입 성공 여부를 저장하는 viewModel 값을 관찰하는 옵저버 등록
        joinStatusObserver()

        // 회원가입 완료 버튼 클릭 이벤트
        binding.joinNextButton.setOnClickListener {
            binding.joinProgressbar.visibility = View.VISIBLE
            binding.ageGirdlayout.visibility = View.INVISIBLE
            val temp = joinViewModel.ageState.value
            val size = temp!!.size


            for (i in 0 until size) {
                if (temp[i]) {
                    joinViewModel.joinUserData.age = (i + 1) * 10
                    break
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                joinViewModel.userJoin()
            }

            // 완전한 회원가입 성공일 경우, 옵저버에 의해서 다음 페이지로 넘어감
        }

    } // End of onViewCreated

    private fun initData() {
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

    private fun joinStatusObserver() {
        joinViewModel.joinResponseStatus.observe(viewLifecycleOwner) {
            // response successful 일 경우
            // token값을 sharedPreference에 저장하고,
            // MainActivity로 자동전환
            if (it) {
                mContext.showToastMessage("회원가입 성공")

                // refreshToken & accessToken
                // savedInstance 저장하기
                Application.sharedPreferencesUtil.addUserToken(
                    Token(
                        joinViewModel.joinSuccessUserData.value!!.access_token.toString(),
                        joinViewModel.joinSuccessUserData.value!!.refresh_token.toString()
                    )
                )

                val intent = Intent(activity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }

            // 실패일 경우, 오류 메시지를 띄움
            if (!it) {
                mContext.showToastMessage("회원가입 실패")
            }
        }
    } // End of joinStatusObserver
} // End of JoinAgeFragment class
