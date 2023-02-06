package com.app.myfoottrip.util

import java.text.DecimalFormat

object CommonUtils {
    fun makeComma(num: Int): String {
        var comma = DecimalFormat("#,###")
        return "${comma.format(num)}개"
    }
}
