package com.app.myfoottrip.ui.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.bumptech.glide.Glide

private const val TAG = "PhotoAdapter_마이풋트립"
class PhotoAdapter(var imageList:List<Uri?>) : RecyclerView.Adapter<PhotoAdapter.PhotoHolder>(){

    inner class PhotoHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val image = itemView.findViewById<ImageView>(R.id.iv_image)
        val deleteBtn = itemView.findViewById<ImageView>(R.id.iv_delete)

        fun bindInfo(imageUrl : Uri?){

            if (imageUrl != null){
                Glide.with(itemView)
                    .load(imageUrl)
                    .centerCrop()
                    .into(image)
            }

            deleteBtn.setOnClickListener{
                itemClickListner.onClick(it, layoutPosition)
            }

            Log.d(TAG, "bindInfo: ${imageUrl}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_photo, parent, false)
        return PhotoHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
        holder.apply{
            bindInfo(imageList[position])
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    //클릭 인터페이스 정의 사용하는 곳에서 만들어준다.
    interface ItemClickListener {
        fun onClick(view: View,  position: Int)
    }

    //클릭리스너 선언
    private lateinit var itemClickListner: ItemClickListener
    //클릭리스너 등록 매소드
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }
}
