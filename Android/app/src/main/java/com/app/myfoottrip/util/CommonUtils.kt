package com.app.myfoottrip.util

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import java.text.DecimalFormat

object CommonUtils {
    fun makeComma(num: Int): String {
        var comma = DecimalFormat("#,###")
        return "${comma.format(num)}개"
    }
    //textview 특정 text 컬러나 스타일 바꾸기
    fun setSpannableText(text: String, content : String) : SpannableString{
        val spannableString = SpannableString(content)
        val start: Int = content.indexOf(text)
        val end = start + text.length

        when(text){
            "BLUE" ->  spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#0080FF")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            "BRONZE" -> spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#A65E44")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            "SILVER" ->spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#AEAEAE")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            "GOLD" ->spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#CFB53B")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            else -> spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#1E3AD9")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }
}
