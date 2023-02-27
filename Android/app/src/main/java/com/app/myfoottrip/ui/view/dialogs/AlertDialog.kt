package com.app.myfoottrip.ui.view.dialogs

import android.app.Dialog
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.app.myfoottrip.databinding.DialogAlertBinding

class AlertDialog(private val context : AppCompatActivity) {
    private lateinit var binding: DialogAlertBinding
    private val dialog = Dialog(context)

    private lateinit var okListener : AlertDialogOKClickedListener
    private lateinit var cancelListener : AlertDialogCancelClickedListener

    fun show(title:String, content : String) {
        binding = DialogAlertBinding.inflate(context.layoutInflater)

//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)   //타이틀바 제거
        dialog.setContentView(binding.root)     //다이얼로그에 사용할 xml 파일을 불러옴
        dialog.setCancelable(false)    //다이얼로그의 바깥 화면을 눌렀을 때 다이얼로그가 닫히지 않도록 함

        val width = (context.resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = WindowManager.LayoutParams.WRAP_CONTENT

        dialog.setTitle("테스트")
        dialog.window?.setLayout(width, height)

        binding.title.text = title
        binding.content.text = content //부모 액티비티에서 전달 받은 내용 세팅

        //ok 버튼 동작
        binding.okBtn.setOnClickListener {
            okListener.onOKClicked()
            dialog.dismiss()
        }

        //취소 버튼 동작
        binding.cancelBtn.setOnClickListener {
            cancelListener.onCancelClicked()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun setOnOKClickedListener(listener: () -> Unit) {
        this.okListener = object: AlertDialogOKClickedListener {
            override fun onOKClicked() {
                listener()
            }
        }
    }

    interface AlertDialogOKClickedListener {
        fun onOKClicked()
    }

    fun setOnCancelClickedListener(listener: () -> Unit) {
        this.cancelListener = object: AlertDialogCancelClickedListener {
            override fun onCancelClicked() {
                listener()
            }
        }
    }

    interface AlertDialogCancelClickedListener {
        fun onCancelClicked()
    }

}