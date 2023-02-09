package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.L
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.ListEditPlaceImageBinding
import com.kakao.sdk.template.model.Link
import retrofit2.http.Url
import java.util.*

class PlaceImageAdapter(val context: Context) :
    RecyclerView.Adapter<PlaceImageAdapter.PlaceImageHolder>() {
    private lateinit var binding: ListEditPlaceImageBinding
    private var tempList: MutableList<Uri> = LinkedList()

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
        holder.itemView.findViewById<ImageView>(R.id.image_imageview)
            .setImageURI(tempList[position])
    } // End of onBindViewHolder

    fun addItem(imageUri: Uri) {
        tempList.add(imageUri)
        notifyDataSetChanged()
    } // End of addList

    fun setList(imgeUriList: MutableList<Uri>) {
        tempList.addAll(imgeUriList)
        notifyDataSetChanged()
    } // End of setList

    fun removeList(index: Int) {
        tempList.removeAt(index)
        notifyDataSetChanged()
    } // End of removeList

    override fun getItemCount() = tempList.size
} // End of PlaceImageAdapter class