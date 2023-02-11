package com.app.myfoottrip.ui.view.travel

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.databinding.ListEditPlaceImageBinding
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import jp.wasabeef.picasso.transformations.CropSquareTransformation


private const val TAG = "PlaceImageAdapter_싸피"

class PlaceImageAdapter(val context: Context, private val imageList: MutableList<Uri>) :
    RecyclerView.Adapter<PlaceImageAdapter.PlaceImageHolder>() {
    private lateinit var binding: ListEditPlaceImageBinding

    //클릭리스너 선언
    private lateinit var itemClickListner: ItemClickListener

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
        val imageView = holder.itemView.findViewById<ImageView>(R.id.image_imageview)
        Picasso.get().load(imageList[position]).transform(CropSquareTransformation()).into(imageView)
    } // End of onBindViewHolder

    inner class CircleTransfrom : Transformation {
        override fun transform(source: Bitmap?): Bitmap {
            var size = Math.min(source!!.width, source.height)

            val x = (source.width - size) / 2
            val y = (source.height - size) / 2

            val squareBitmap = Bitmap.createBitmap(source, x, y, size, size)
            if (squareBitmap != source) {
                source.recycle()
            }

            val bitmap = Bitmap.createBitmap(size, size, source.config)
            val canvas = Canvas(bitmap)
            val paint = Paint()
            val shader = BitmapShader(
                squareBitmap,
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP
            )
            paint.shader = shader
            paint.isAntiAlias = true

            val r: Float = size / 2f
            canvas.drawCircle(r, r, r, paint)

            squareBitmap.recycle()
            return bitmap
        }

        override fun key(): String {
            return "circle"
        }
    } // End of CircleTransfrom

    interface ItemClickListener {
        fun onAddImageButtonClicked(position: Int, imageUri : Uri) // 이미지를 추가해주는 메소드
        fun onRemoveImageButtonClicked(position : Int) // 이미지 삭제 버튼 눌렀을 때 메소드,
    } // End of ItemClickListener interface

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    } // End of setItemClickListener

    internal fun addData(newImageUri : Uri) {
        imageList.add(newImageUri)
        notifyDataSetChanged()
    } // End of addData

    override fun getItemCount() = imageList.size
} // End of PlaceImageAdapter class