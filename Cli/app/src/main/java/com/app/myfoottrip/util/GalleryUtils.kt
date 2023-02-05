package com.app.myfoottrip.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val TAG = "GalleryUtils_마이풋트립"
object GalleryUtils {

    private val fbStore = FirebaseStorage.getInstance().reference

    fun getGallery(context : Context, imageLauncher : ActivityResultLauncher<Intent>) { //사진 가져오기
        getPermission(object : PermissionListener {
            override fun onPermissionGranted() {
                val intent = Intent(Intent.ACTION_PICK)
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                imageLauncher.launch(intent)
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(context, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getPermission(listener: PermissionListener){
        TedPermission.create()
            .setPermissionListener(listener)
            .setDeniedMessage("권한을 허용해주세요")
            .setPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }

    fun getCamera(context : Context, imageLauncher : ActivityResultLauncher<Intent>) { //카메라로 촬영하기
        getCameraPermission(object : PermissionListener {
            override fun onPermissionGranted() {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                imageLauncher.launch(intent)
            }
            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(context, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getCameraPermission(listener: PermissionListener){
        TedPermission.create()
            .setPermissionListener(listener)
            .setDeniedMessage("권한을 허용해주세요")
            .setPermissions(android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
    }

    fun getImageUri(context : Context, bitmap : Bitmap): Uri? { //bitmap을 uri로 변환하는 함수
        val byte = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byte)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver,
            bitmap, "IMAGE_${System.currentTimeMillis()}", null)
        return Uri.parse(path)
    }

    suspend fun insertImage(url : ArrayList<String>, imgUri : ArrayList<Uri>,type : Int,boardId: Int) : ArrayList<String>{ //TODO : 실패했을 경우 처리
        return withContext(Dispatchers.IO){
            val list = arrayListOf<String>()
            for(i in 0 until url.size){
                //서버 이미지이면 안 변경해줘도 됨
                if (imgUri[i].toString().startsWith("https")) {
                    list.add(imgUri[i].toString())
                }else{
                    //보드타입인지 프로필타입인지 지정
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d(TAG, "이미지 저장 성공~~~~~~~~~~$i\n image : ${url[i]}")
                        var direction = ""
                        when(type){
                            0 ->{
                                direction = "board"
                            }
                            1 ->{
                                direction = "profile"
                            }
                        }
                        if(type == 0) {
                            val imageRef = fbStore.child("image/$direction/${boardId}/${url[i]}")
                            list.add(imageRef.putFile(imgUri[i]).await().storage.downloadUrl.await().toString())
                        } else {
                            val imageRef = fbStore.child("image/$direction/${url[i]}")
                            list.add(imageRef.putFile(imgUri[i]).await().storage.downloadUrl.await().toString())
                        }
                    }.join()
                }
            }
            list
        }
    }
}