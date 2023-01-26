package com.app.myfoottrip.ui.view.start

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.app.myfoottrip.R
import com.app.myfoottrip.data.viewmodel.KakaoAuthViewModel
import com.app.myfoottrip.databinding.FragmentStartBinding
import com.app.myfoottrip.ui.base.BaseFragment
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "StartFragment_싸피"

class StartFragment : BaseFragment<FragmentStartBinding>(
    FragmentStartBinding::bind, R.layout.fragment_start
) {
    private val kakaoAuthViewModel: KakaoAuthViewModel by viewModels()
    private var email: String = ""
    private var phone: String = ""
    private var name: String = ""
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    } // End of onAttach

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnLogin.setOnClickListener {
                showLoginFragment()
            }

            btnNaver.setOnClickListener {
                //현재 토큰을 가지고 있다면 삭제함
                deleteNaverToken()
                naverLogin()
            }
        }

        // 회원가입 텍스트 클릭시 회원가입 페이지로 이동
        binding.tvJoin.setOnClickListener {
            Navigation.findNavController(binding.tvJoin)
                .navigate(R.id.action_startFragment_to_emailJoinFragment)
        }


        // 카카오 로그인 버튼 클릭 이벤트
        binding.btnKakao.setOnClickListener {
            kakaoLogin()
        }

        // 구글 로그인 버튼 클릭 이벤트
        binding.btnGoogle.setOnClickListener {

        }

    } // End of onViewCreatedkakaoLogin

    private fun kakaoLogin() {

        // 카카오톡으로 로그인
        UserApiClient.instance.loginWithKakaoTalk(mContext) { token, error ->
            if (error != null) {
                Log.e(TAG, "로그인 실패", error)
            } else if (token != null) {
//                Log.i(TAG, "로그인 성공 ${token.accessToken}")
//                Log.d(TAG, "kakaoLogin: ${token.idToken}")
                kakaoLoginGetUserData()
            }
        }

        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(mContext)) {
            UserApiClient.instance.loginWithKakaoTalk(mContext) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(mContext, callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(mContext, callback = callback)
        }

    } // End of kakao Login

    private fun kakaoLoginGetUserData() {
        // 사용자 정보 요청 (기본)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                Log.i(
                    TAG, "사용자 정보 요청 성공" +
                            "\n회원번호: ${user.id}" +
                            "\n이메일: ${user.kakaoAccount?.email}" +
                            "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                            "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}" +
                            "\n이름 : ${user.kakaoAccount?.name}" +
                            "${user.hasSignedUp}"
                )
            }
        }
    } // End of kakaoLoginGetUserData


    private fun naverLogin() {
        val oAuthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                // 네이버 로그인 API 호출 성공 시 유저 정보를 가져온다
                NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
                    override fun onSuccess(result: NidProfileResponse) {
                        name = result.profile?.name.toString()
                        email = result.profile?.email.toString()
                        phone = result.profile?.mobile.toString()
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
    } // End of naverLogin

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

    private fun showLoginFragment() {
        findNavController().navigate(R.id.action_userFragment_to_loginFragment)
    } // End of showLoginFragment

} // End of StartFragment class
