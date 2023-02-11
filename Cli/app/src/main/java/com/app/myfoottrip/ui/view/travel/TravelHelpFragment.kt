package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.FragmentTravelHelpBinding
import com.app.myfoottrip.ui.base.BaseFragment
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

private const val TAG = "TravelHelpFragment_마이풋트립"

class TravelHelpFragment : BaseFragment<FragmentTravelHelpBinding> (
    FragmentTravelHelpBinding::bind, R.layout.fragment_travel_help
) {

    private lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    } // End of onAttach

    override fun onViewCreated(view:View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // 뒤로가기
            ivBack.setOnClickListener { findNavController().popBackStack() }

            // 슬라이더
            carouselTravelHelp.registerLifecycle(viewLifecycleOwner)
            var list = mutableListOf<CarouselItem>().let {
                it.apply {
                    add(CarouselItem(R.drawable.travel_help_1))
                    add(CarouselItem(R.drawable.travel_help_2))
                    add(CarouselItem(R.drawable.travel_help_3))
                    add(CarouselItem(R.drawable.travel_help_4))
                    add(CarouselItem(R.drawable.travel_help_5))
                    add(CarouselItem(R.drawable.travel_help_6))
                    add(CarouselItem(R.drawable.travel_help_7))
                }
            }
            carouselTravelHelp.setData(list)

        }
    } // End of onViewCreated

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    } // End of onDetach
}