package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.ListEditPlaceImageBinding
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

private const val TAG = "PlaceImageAdapter_싸피"

class PlaceImageAdapter(val context: Context, private val imageList: MutableList<Uri>) :
    RecyclerView.Adapter<PlaceImageAdapter.PlaceImageHolder>() {
    private lateinit var binding: ListEditPlaceImageBinding

    inner class PlaceImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindInfo(data: Uri) {

        }
    } // End of PlaceImageHolder inner class

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceImageHolder {
        binding = ListEditPlaceImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaceImageHolder(binding.root)
    } // End of onCreateViewHolder

    override fun onBindViewHolder(holder: PlaceImageHolder, position: Int) {
        Log.d(TAG, "이미지 잘 오나 ?: ${imageList[position]}")

        val imageView = holder.itemView.findViewById<ImageView>(R.id.image_imageview)

        Picasso.get().load(Uri.parse(imageList[position].toString()))
            .networkPolicy(NetworkPolicy.OFFLINE)
            .into(imageView)
    } // End of onBindViewHolder

//    fun addItem(imageUri: Uri) {
//        imageList.add(imageUri)
//        notifyDataSetChanged()
//    } // End of addList

//    fun setList(imgeUriList: MutableList<Uri>) {
//        tempList.addAll(imgeUriList)
//        notifyDataSetChanged()
//    } // End of setList

//    fun removeList(index: Int) {
//        tempList.removeAt(index)
//        notifyDataSetChanged()
//    } // End of removeList

    override fun getItemCount() = imageList.size
} // End of PlaceImageAdapter class