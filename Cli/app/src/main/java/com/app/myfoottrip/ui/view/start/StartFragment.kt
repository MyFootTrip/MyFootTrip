package com.app.myfoottrip.ui.view.start

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.FragmentStartBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse

private const val TAG = "StartFragment_마이풋트립"

class StartFragment : BaseFragment<FragmentStartBinding>(
    FragmentStartBinding::bind, R.layout.fragment_start
) {
    private var email: String = ""
    private var phone: String = ""
    private var name: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // toast 예시
        showToast("test", ToastType.WARNING, true)
        // fragment

        binding.apply {
            btnLogin.setOnClickListener {
                showLoginFragment()
            }

            btnNaver.setOnClickListener {
                //현재 토큰을 가지고 있다면 삭제함
                deleteNaverToken()

                val oAuthLoginCallback = object : OAuthLoginCallback {
                    override fun onSuccess() {
                        // 네이버 로그인 API 호출 성공 시 유저 정보를 가져온다
                        NidOAuthLogin().callProfileApi(object :
                            NidProfileCallback<NidProfileResponse> {
                            override fun onSuccess(result: NidProfileResponse) {
                                name = result.profile?.name.toString()
                                email = result.profile?.email.toString()
                                phone = result.profile?.mobile.toString()
                                Log.e(TAG, "네이버 로그인한 유저 정보 - 이름 : $name")
                                Log.e(TAG, "네이버 로그인한 유저 정보 - 이메일 : $email")
                                Log.e(TAG, "네이버 로그인한 유저 정보 - 전화번호 : $phone")
                            }

                            override fun onError(errorCode: Int, message: String) {
                                //
                            }

                            override fun onFailure(httpStatus: Int, message: String) {
                                //
                            }
                        })
                    }

                    override fun onError(errorCode: Int, message: String) {
                        val naverAccessToken = NaverIdLoginSDK.getAccessToken()
                        Log.e(TAG, "naverAccessToken : $naverAccessToken")
                    }

                    override fun onFailure(httpStatus: Int, message: String) {
                        //
                    }
                }

                NaverIdLoginSDK.initialize(
                    requireContext(),
                    getString(R.string.naver_client_id),
                    getString(R.string.naver_client_secret),
                    "앱 이름"
                )
                NaverIdLoginSDK.authenticate(requireContext(), oAuthLoginCallback)
            }
        }

        
        // 회원가입 텍스트 클릭시 회원가입 페이지로 이동
        binding.tvJoin.setOnClickListener {
            Navigation.findNavController(binding.tvJoin)
                .navigate(R.id.action_startFragment_to_emailJoinFragment)
        }
    }


    private fun showLoginFragment() {
        findNavController().navigate(R.id.action_userFragment_to_loginFragment)
    }

    private fun deleteNaverToken() {
        NidOAuthLogin().callDeleteTokenApi(requireContext(), object : OAuthLoginCallback {
            override fun onSuccess() {
                //서버에서 토큰 삭제에 성공한 상태입니다.
                Toast.makeText(requireContext(), "네이버 아이디 토큰삭제 성공!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                Log.d("naver", "errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}")
                Log.d("naver", "errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}")
            }

            override fun onError(errorCode: Int, message: String) {
                // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                onFailure(errorCode, message)
            }
        })
    }
}