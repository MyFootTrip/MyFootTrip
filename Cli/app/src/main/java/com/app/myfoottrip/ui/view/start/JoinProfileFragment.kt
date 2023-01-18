package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.app.myfoottrip.R
import com.app.myfoottrip.data.model.viewmodel.JoinViewModel
import com.app.myfoottrip.databinding.FragmentJoinProfileBinding
import com.app.myfoottrip.ui.view.dialogs.ServiceClauseCustomDialog

class JoinProfileFragment : Fragment() {
    private lateinit var mContext: Context
    private lateinit var binding: FragmentJoinProfileBinding
    private val joinViewModel by activityViewModels<JoinViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    } // End of onCreate

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinProfileBinding.inflate(inflater, container, false)
        return binding.root
    } // End of onCreateView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.joinProfileImagePlusButton.setOnClickListener {
            Toast.makeText(mContext, "테스트 입니다.", Toast.LENGTH_SHORT).show()
        }

        // 다음 버튼 클릭 이벤트
        binding.joinNextButton.setOnClickListener {
            joinViewModel.joinUserData.username = binding.editTextJoinName.text.toString()
            joinViewModel.joinUserData.nickname = binding.editTextJoinNickname.text.toString()

            Navigation.findNavController(binding.joinNextButton)
                .navigate(R.id.action_joinProfileFragment_to_joinAgeFragment)
        }
    }

} // End of JoinProfileFragment class
