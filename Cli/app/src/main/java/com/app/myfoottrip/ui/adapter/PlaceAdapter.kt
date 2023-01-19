package com.app.myfoottrip.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Place

class PlaceAdapter(var placeList:List<Place>) : RecyclerView.Adapter<PlaceAdapter.PlaceHolder>(){

    inner class PlaceHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val placeId = itemView.findViewById<TextView>(R.id.iv_place_order)
        val placeName = itemView.findViewById<TextView>(R.id.tv_place_name)
        val saveDate = itemView.findViewById<TextView>(R.id.tv_place_save_date)

        fun bindInfo(place : Place){
            placeId.text = place.placeId.toString()
            placeName.text = place.placeName
            saveDate.text = place.saveDate.toString()

            itemView.setOnClickListener{
                itemClickListner.onClick(it, layoutPosition,place.placeId!!)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_place, parent, false)
        return PlaceHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.apply{
            bindInfo(placeList[position])
        }
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    //클릭 인터페이스 정의 사용하는 곳에서 만들어준다.
    interface ItemClickListener {
        fun onClick(view: View,  position: Int, placeId : Int)
    }

    //클릭리스너 선언
    private lateinit var itemClickListner: ItemClickListener
    //클릭리스너 등록 매소드
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }
}
