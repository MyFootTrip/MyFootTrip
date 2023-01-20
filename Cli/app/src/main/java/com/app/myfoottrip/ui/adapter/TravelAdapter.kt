package com.app.myfoottrip.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.myfoottrip.R
import com.app.myfoottrip.data.dto.Travel
import com.app.myfoottrip.databinding.ListItemTravelBinding
import com.app.myfoottrip.util.TimeUtils
import com.bumptech.glide.Glide

// 0 : 여정 선택, 1: 여정 보기
class TravelAdapter(private var travelList : ArrayList<Travel> = arrayListOf(),
                    private val type : Int = 1) : RecyclerView.Adapter<TravelAdapter.TravelHolder>() {
    private var selected: Int = -1

    fun getSelected(): Int{ return selected }
    fun setSelected(position : Int){ selected = position }
    fun setList(list : ArrayList<Travel>){ travelList = list }

    inner class TravelHolder(private val binding : ListItemTravelBinding) : RecyclerView.ViewHolder(binding.root){
        fun bindInfo(position : Int, travelDto : Travel){
            binding.apply {
                if(type == 1){ //여정 선택
                    chipTravelSelect.text = "삭제"
                }else{ //여정 삭제
                    chipTravelSelect.text = "선택"
                }

                chipTravelSelect.isChecked = (selected == position) //선택은 하나만 하도록

                Glide.with(itemView)
                    .load("https://a.cdn-hotels.com/gdcs/production85/d946/73f139d8-4c1d-4ef6-97b0-9b2ccf29878a.jpg?impolicy=fcrop&w=800&h=533&q=medium")
                    .centerCrop()
                    .into(ivTravel)

                tvTravelName.text = travelDto.location.joinToString(", ")
                tvTravelDate.text = "${TimeUtils.getDateString(travelDto.startDate)} - ${TimeUtils.getDateString(travelDto.endDate)}"

                clTravelItem.setOnClickListener {
                    itemClickListner.onAllClick(position, travelDto)
                }
                chipTravelSelect.setOnClickListener{
                    itemClickListner.onChipClick(type,position,travelDto)
                }
            }
        }
    }

    interface ItemClickListener{
        fun onAllClick(position: Int, travelDto: Travel) //전체 클릭한 경우
        fun onChipClick(type : Int, position: Int, travelDto: Travel) //chip만 클릭한 경우
    }

    //클릭리스너 선언
    private lateinit var itemClickListner: ItemClickListener
    //클릭리스너 등록 매소드
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_travel,parent,false)
        return TravelHolder(ListItemTravelBinding.bind(view))
    }

    override fun onBindViewHolder(holder: TravelHolder, position: Int) {
        holder.bindInfo(position,travelList[position])
    }

    override fun getItemCount(): Int  = travelList.size
}