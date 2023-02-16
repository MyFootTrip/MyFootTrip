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
import com.bumptech.glide.load.engine.DiskCacheStrategy

private const val TAG = "PhotoAdapter_마이풋트립"
class PhotoAdapter(var imageList:List<Uri>) : RecyclerView.Adapter<PhotoAdapter.PhotoHolder>(){

    inner class PhotoHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val image = itemView.findViewById<ImageView>(R.id.iv_image)
        val deleteBtn = itemView.findViewById<ImageView>(R.id.iv_delete)
        val checkUri = Uri.parse("https://images.velog.io/images/ccmmss98/post/4de24da3-70a1-4a57-8df8-7d8bd8ef2b70/saffy.png")

        fun bindInfo(imageUrl : Uri){

            if (imageUrl != checkUri){
                Glide.with(itemView).load(imageUrl).skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(image)
            }

            deleteBtn.setOnClickListener{
                itemClickListner.onClick(it, layoutPosition)
            }
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
