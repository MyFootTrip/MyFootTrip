package com.app.myfoottrip.ui.view.mypage

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.TestViewModel
import com.app.myfoottrip.databinding.FragmentTestBinding
import com.app.myfoottrip.ui.adapter.HomeAdapter
import com.app.myfoottrip.ui.adapter.TestPagingDataAdapter
import com.app.myfoottrip.ui.base.BaseFragment
import com.app.myfoottrip.ui.view.main.MainActivity

private const val TAG = "TestFragment_마이풋트립"
class TestFragment : BaseFragment<FragmentTestBinding>(
    FragmentTestBinding::bind,R.layout.fragment_test
) {

    private lateinit var callback: OnBackPressedCallback
    private lateinit var mainActivity: MainActivity
    private val testAdapter by lazy { TestPagingDataAdapter() }

    private val testViewModel by activityViewModels<TestViewModel>()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    } // End of onAttach

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivBack.setOnClickListener {
                findNavController().popBackStack()

            }
            rvTest.adapter = testAdapter
            rvTest.setHasFixedSize(true)

            testAdapter.setItemClickListener(object : TestPagingDataAdapter.ItemClickListener {
                override fun onClick(view: View, position: Int, boardId: Int) {
//                    boardViewModel.boardId = boardId
//                    binding.spinnerSort.dismiss()
//                    findNavController().navigate(R.id.action_mainFragment_to_boardFragment)
//                    navigationViewModel.type = 0
                    Log.d(TAG, "게시물 클릭: ${boardId}가 클릭됨")
                }
            })
        }


        testViewModel.result.observe(viewLifecycleOwner){
            testAdapter.submitData(this.lifecycle,it)
            Log.d(TAG, "onViewCreated: 호출됐음")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }
}