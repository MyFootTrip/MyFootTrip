package com.app.myfoottrip.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

class ChangeMultipartUtil {

    fun changeAbsolutelyPath(path: Uri?, context: Context): String {
        var proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        var c: Cursor? = context.contentResolver.query(path!!, proj, null, null, null)
        var index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()

        var result = c?.getString(index!!)
        return result!!
    } // End of absolutelyPath
} // End of ChangeMultipartUtil class