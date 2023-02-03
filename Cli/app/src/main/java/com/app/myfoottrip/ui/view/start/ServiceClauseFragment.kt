package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.JoinViewModel
import com.app.myfoottrip.data.viewmodel.NavigationViewModel
import com.app.myfoottrip.databinding.FragmentServiceClauseBinding


private const val TAG = "ServiceClauseFragment_싸피"

class ServiceClauseFragment : Fragment() {
    private lateinit var mContext: Context
    private lateinit var binding: FragmentServiceClauseBinding
    private lateinit var joinBackButtonCustomView: JoinBackButtonCustomView
    private val joinViewModel by activityViewModels<JoinViewModel>()
    private lateinit var callback: OnBackPressedCallback

    private val navigationViewModel by activityViewModels<NavigationViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "handleOnBackPressed: ")
                navigationViewModel.startPage = 1
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    } // End of onAttach

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentServiceClauseBinding.inflate(inflater, container, false)
        return binding.root
    } // End of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        joinBackButtonCustomView = binding.joinBackButtonCustomview

        // 모두 동의 체크 여부에 따른 체크 상태 변화 메소드
        clauseCheckSwitch()

        // 동의 여부에 따른 버튼 상태 변화
        checkStateByButtonStateObserver()

        /*
            모두 동의가 클릭되면 전체 상태에 체크되어야 함
            전체 동의가 활성화되고, 비활성화 됨에 따라 아래의 3가지 항목들도 체크 상태가 변해야함
            만약 모두 동의된 상태에서 다른 항목 하나라도 체크가 되지 않는다면, 전체 동의 체크는 비활성화 되어야함
         */

        binding.thirdServiceClauseDetailTextButton.setOnClickListener {
            // 약관 동의 다이얼로그 나오기
            val mainAct = requireActivity() as StartActivity
            mainAct.showServiceDialog()
        }

        binding.fourthServiceClauseDetailTextButton.setOnClickListener {
            // 약관 동의 다이얼로그 나오기
            val mainAct = requireActivity() as StartActivity
            mainAct.showServiceDialog()
        }

        // 다음 버튼 클릭시 프레그먼트 전환
        binding.joinNextButton.setOnClickListener {
            Navigation.findNavController(binding.joinNextButton)
                .navigate(R.id.action_serviceClauseFragment_to_emailJoinFragment)
        }

        joinBackButtonCustomView.findViewById<AppCompatButton>(R.id.custom_back_button_appcompatbutton)
            .setOnClickListener {
                Log.d(TAG, "joinBackButtonCustomView onClick: ")
                navigationViewModel.startPage = 1
                findNavController().popBackStack()
            }

    } // End of onViewCreated

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    } // End of onDetach

    private fun checkStateByButtonStateObserver() {
        joinViewModel.isAllCheckedCount.observe(viewLifecycleOwner) {
            if (joinViewModel.isAllCheckedCount.value == 3) {
                binding.joinNextButton.isClickable = true
                binding.joinNextButton.isEnabled = true
            }

            if (joinViewModel.isAllCheckedCount.value != 3) {
                binding.joinNextButton.isClickable = false
                binding.joinNextButton.isEnabled = false
            }
        }
    } // End of checkStateByButtonState

    private fun clauseCheckSwitch() {
        binding.firstClauseTv.setOnClickListener {
            binding.firstCheckbox.isChecked = !binding.firstCheckbox.isChecked
        }

        binding.secondClauseTv.setOnClickListener {
            binding.secondCheckbox.isChecked = !binding.secondCheckbox.isChecked
        }

        binding.thirdClauseTv.setOnClickListener {
            binding.thirdCheckbox.isChecked = !binding.thirdCheckbox.isChecked
        }

        binding.fourthClauseTv.setOnClickListener {
            binding.fourthCheckbox.isChecked = !binding.fourthCheckbox.isChecked
        }


        if (!binding.firstCheckbox.isChecked && binding.secondCheckbox.isChecked && binding.thirdCheckbox.isChecked && binding.fourthCheckbox.isChecked) {
            binding.firstCheckbox.isChecked = true
        }

        // 전체 동의 체크 버튼
        binding.firstCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.secondCheckbox.isChecked = true
                binding.thirdCheckbox.isChecked = true
                binding.fourthCheckbox.isChecked = true
            }

            if (!isChecked && binding.secondCheckbox.isChecked && binding.thirdCheckbox.isChecked && binding.fourthCheckbox.isChecked) {
                binding.secondCheckbox.isChecked = false
                binding.thirdCheckbox.isChecked = false
                binding.fourthCheckbox.isChecked = false
            }

            allCheckCounting()
        }

        // 2번째 항목 체크 버튼
        binding.secondCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->

            if (binding.firstCheckbox.isChecked && !isChecked) {
                binding.firstCheckbox.isChecked = false
            }

            if (!binding.firstCheckbox.isChecked && binding.thirdCheckbox.isChecked && binding.fourthCheckbox.isChecked && isChecked) {
                binding.firstCheckbox.isChecked = true
            }

            allCheckCounting()
        }

        // 3번째 항목 체크 버튼
        binding.thirdCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (binding.firstCheckbox.isChecked && !isChecked) {
                binding.firstCheckbox.isChecked = false
            }

            if (!binding.firstCheckbox.isChecked && binding.secondCheckbox.isChecked && binding.fourthCheckbox.isChecked && isChecked) {
                binding.firstCheckbox.isChecked = true
            }

            allCheckCounting()
        }


        // 4번째 항목 체크 버튼
        binding.fourthCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (binding.firstCheckbox.isChecked && !isChecked) {
                binding.firstCheckbox.isChecked = false
            }

            if (!binding.firstCheckbox.isChecked && binding.secondCheckbox.isChecked && binding.thirdCheckbox.isChecked && isChecked) {
                binding.firstCheckbox.isChecked = true
            }

            allCheckCounting()
        }
    } // End of clauseCheckSystem

    private fun allCheckCounting() {
        // 전체 체크 세부 항목의 개수가 몇개인지 파악하는 메소드
        var count = 0

        if (binding.firstCheckbox.isChecked) {
            count++
        }

        if (binding.secondCheckbox.isChecked) {
            count++
        }

        if (binding.thirdCheckbox.isChecked) {
            count++
        }

        joinViewModel.setCheckedCountValue(count)
    } // End of allCheckCounting
} // End of ServiceClauseFragment
