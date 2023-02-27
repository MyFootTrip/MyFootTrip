package com.app.myfoottrip.util

import android.content.Context
import android.graphics.Point
import android.view.WindowManager

object DeviceSizeUtil {

    fun deviceSizeCheck(context: Context): Point {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    } // End of deviceSizeCheck

}