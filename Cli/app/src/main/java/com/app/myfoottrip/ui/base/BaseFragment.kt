package com.app.myfoottrip.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import es.dmoral.toasty.Toasty


// Fragment의 기본을 작성, 뷰 바인딩 활용
typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<B : ViewBinding>(
    private val bind: (View) -> B,
    @LayoutRes layoutResId: Int
) : Fragment(layoutResId) { // End of BaseFragment class
    private var _binding: B? = null
    val binding get() = _binding ?: throw IllegalStateException("binding fail")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bind(super.onCreateView(inflater, container, savedInstanceState)!!)
        return binding.root
    } // End of onCreateView

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    } // End of onDestroyView

    //예쁜 토스트 함수 message, type, icon 유무를 입력하여 사용
    fun showToast(message: String, type: ToastType? = null, iconEnable: Boolean = true) {
        when (type) {
            ToastType.CUSTOM -> Toasty.warning(requireContext(), message, Toast.LENGTH_SHORT).show()
            ToastType.INFO -> Toasty.info(requireContext(), message, Toast.LENGTH_SHORT, iconEnable)
                .show()
            ToastType.ERROR -> Toasty.error(
                requireContext(),
                message,
                Toast.LENGTH_SHORT,
                iconEnable
            ).show()
            ToastType.SUCCESS -> Toasty.success(
                requireContext(),
                message,
                Toast.LENGTH_SHORT,
                iconEnable
            ).show()
            ToastType.WARNING -> Toasty.warning(
                requireContext(),
                message,
                Toast.LENGTH_SHORT,
                iconEnable
            ).show()
            else -> Toasty.normal(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    } // End of showToast

    enum class ToastType { ERROR, SUCCESS, INFO, WARNING, BASIC, CUSTOM }
}
