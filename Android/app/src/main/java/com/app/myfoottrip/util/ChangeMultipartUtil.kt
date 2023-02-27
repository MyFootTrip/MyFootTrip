package com.app.myfoottrip.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

class ChangeMultipartUtil {
    fun changeAbsolutelyPath(path: Uri?, context: Context): String {
        val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val c: Cursor? = context.contentResolver.query(path!!, proj, null, null, null)
        val index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()

        var result = c?.getString(index!!)
        return result!!
    } // End of absolutelyPath
} // End of ChangeMultipartUtil class
