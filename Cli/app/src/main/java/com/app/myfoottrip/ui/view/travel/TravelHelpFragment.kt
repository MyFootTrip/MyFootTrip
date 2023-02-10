package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            ivCancelBtn.setOnClickListener {findNavController().popBackStack() }

            // 슬라이더
            carouselTravelHelp.registerLifecycle(viewLifecycleOwner)
            var list = mutableListOf<CarouselItem>().let {
                it.apply {
                    add(CarouselItem(R.drawable.default_image))
                    add(CarouselItem(R.drawable.default_image))
                }
            }
            carouselTravelHelp.setData(list)

        }
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    } // End of onDetach
}