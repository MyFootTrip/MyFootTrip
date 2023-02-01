package com.app.myfoottrip.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Place
import com.app.myfoottrip.util.TimeUtils

private const val TAG = "PlaceAdapter_마이풋트립"
class PlaceAdapter(var placeList:List<Place>) : RecyclerView.Adapter<PlaceAdapter.PlaceHolder>(){

    inner class PlaceHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val placeOrder = itemView.findViewById<TextView>(R.id.iv_place_order)
        val placeName = itemView.findViewById<TextView>(R.id.tv_place_name)
        val saveDate = itemView.findViewById<TextView>(R.id.tv_place_save_date)

        fun bindInfo(place : Place){
            placeOrder.text = "${layoutPosition+1}"
            placeName.text = place.placeName
            saveDate.text = TimeUtils.getDateString(place.saveDate!!)

            itemView.setOnClickListener{
                itemClickListner.onClick(it, layoutPosition,place)
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
        fun onClick(view: View,  position: Int, place : Place)
    }

    //클릭리스너 선언
    private lateinit var itemClickListner: ItemClickListener
    //클릭리스너 등록 매소드
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }
}
