package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import retrofit2.http.Url

class PlaceImageAdapter(val context: Context, private val imageList: List<Uri>) :
    RecyclerView.Adapter<PlaceImageAdapter.PlaceImageHolder>() {

    inner class PlaceImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindInfo(data: Uri) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceImageHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: PlaceImageHolder, position: Int) {
       holder.itemView.findViewById<ImageView>(R.id.image_imageview).setImageURI(imageList[position])
    }

    override fun getItemCount() = imageList.size
}